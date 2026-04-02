from flask import Flask, jsonify, request

app = Flask(__name__)

# ✅ GET - 단순 데이터 반환
@app.route("/api/hello", methods=["GET"])
def hello():
    return jsonify({
        "name": "홍길동",
        "age": 30,
        "message": "Hello from Flask!"
    })

# ✅ POST - 받은 데이터 그대로 응답
@app.route("/api/echo", methods=["POST"])
def echo():
    data = request.get_json()
    print(f"[Flask] 받은 데이터: {data}")
    return jsonify({
        "received": data,
        "status": "success"
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
    