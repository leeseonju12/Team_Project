from flask import Flask
from flask_cors import CORS
from config.settings import Config
from controller.image_controller import image_bp
from dotenv import load_dotenv
import logging

load_dotenv()
logger = logging.getLogger(__name__)

def create_app():
    app = Flask(__name__)
    
    # CORS 설정 추가 (모든 경로와 모든 출처에 대해 허용)
    CORS(app)
    
    # 설정 초기화
    Config.init_cloudinary()
    
    # Blueprint 등록
    app.register_blueprint(image_bp, url_prefix='/api/ai')
    
    return app

if __name__ == "__main__":
    app = create_app()
    logger.info("Flask app starting with description prompt support enabled")
    app.run(host="0.0.0.0", port=5000, debug=True)
    
    
# .venv\Scripts\Activate
