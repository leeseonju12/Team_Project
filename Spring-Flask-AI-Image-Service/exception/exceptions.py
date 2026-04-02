class AIProxyException(Exception):
    def __init__(self, message, step, status_code=502):
        super().__init__(message)
        self.message = message
        self.step = step
        self.status_code = status_code

class TranslationError(AIProxyException): pass
class GenerationError(AIProxyException): pass
class UploadError(AIProxyException): pass