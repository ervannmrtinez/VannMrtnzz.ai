import os
from flask import Flask, request, jsonify
import requests

app = Flask(__name__)

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "").strip()
OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-6-astra").strip()
APP_API_TOKEN = os.getenv("APP_API_TOKEN", "").strip()
OPENAI_URL = "https://api.openai.com/v1/responses"

SYSTEM_INSTRUCTIONS = (
    "You are VannMrtnz.AI, a helpful, clear, friendly AI assistant. "
    "Reply in the language used by the user. Be practical and accurate."
)

@app.get("/health")
def health():
    return jsonify({"ok": True, "service": "VannMrtnz.AI"})

@app.post("/chat")
def chat():
    if APP_API_TOKEN:
        supplied = request.headers.get("X-App-Token", "")
        if supplied != APP_API_TOKEN:
            return jsonify({"error": "Unauthorized. Check app token configuration."}), 401

    if not OPENAI_API_KEY:
        return jsonify({"error": "Backend belum dikonfigurasi: OPENAI_API_KEY belum diatur."}), 503

    body = request.get_json(silent=True) or {}
    messages = body.get("messages")
    if not isinstance(messages, list) or not messages:
        return jsonify({"error": "Kirim array 'messages' yang tidak kosong."}), 400

    # Keep request size bounded and accept only user/assistant text messages.
    clean_messages = []
    for item in messages[-24:]:
        if not isinstance(item, dict):
            continue
        role = item.get("role")
        content = item.get("content")
        if role not in ("user", "assistant") or not isinstance(content, str):
            continue
        content = content.strip()
        if content:
            clean_messages.append({"role": role, "content": content[:12000]})

    if not clean_messages:
        return jsonify({"error": "Tidak ada pesan yang valid."}), 400

    payload = {
        "model": OPENAI_MODEL,
        "instructions": SYSTEM_INSTRUCTIONS,
        "input": clean_messages,
        "max_output_tokens": 1200
    }
    try:
        response = requests.post(
            OPENAI_URL,
            headers={
                "Authorization": f"Bearer {OPENAI_API_KEY}",
                "Content-Type": "application/json"
            },
            json=payload,
            timeout=90
        )
        if response.status_code >= 400:
            try:
                err = response.json().get("error", {}).get("message", "Permintaan AI gagal.")
            except Exception:
                err = "Permintaan AI gagal."
            return jsonify({"error": err}), response.status_code

        data = response.json()
        answer = data.get("output_text")
        if not answer:
            chunks = []
            for output in data.get("output", []):
                for content in output.get("content", []):
                    if content.get("type") == "output_text":
                        chunks.append(content.get("text", ""))
            answer = "\n".join(chunks).strip()

        if not answer:
            return jsonify({"error": "Model tidak mengembalikan teks."}), 502
        return jsonify({"reply": answer})
    except requests.Timeout:
        return jsonify({"error": "Waktu tunggu habis. Coba kirim ulang."}), 504
    except requests.RequestException:
        return jsonify({"error": "Tidak bisa terhubung ke layanan AI."}), 502

if __name__ == "__main__":
    port = int(os.getenv("PORT", "8000"))
    app.run(host="0.0.0.0", port=port, debug=False)
