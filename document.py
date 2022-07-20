import numpy as np
import base64
import cv2

class DocumentScanner:

    def __init__( self , image_bytes ):
        self.__image_bytes = image_bytes

    def __convert_image_bytes(self):
        base64.b64decode( self.__image_bytes )


