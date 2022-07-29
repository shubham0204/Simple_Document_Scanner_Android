from fastapi import FastAPI , File , UploadFile
from fastapi.responses import Response
from document import get_rect , get_rect_2 , get_binarized_img
import numpy as np
import cv2

app = FastAPI()

# POST method to get the rect of the cropped document
# It requires an `image` in the body of the request
# FastAPI docs : 1. https://fastapi.tiangolo.com/tutorial/body
#                2. https://fastapi.tiangolo.com/tutorial/request-files
@app.post( "/get_rect" )
async def show_image( image : UploadFile = File() ):
    contents = await image.read()
    # Converting the `contents` bytes to an OpenCV Mat
    # Refer this SO answer -> https://stackoverflow.com/a/61345230/13546426
    img = cv2.imdecode( np.fromstring( contents, np.uint8 ), cv2.IMREAD_COLOR)
    rect = get_rect_2( img )
    return rect


@app.post( "/binarize" )
async def binarize( image : UploadFile = File() ):
    contents = await image.read()
    img = cv2.imdecode(np.fromstring(contents, np.uint8), cv2.IMREAD_COLOR)
    img = get_binarized_img( img )
    img_bytes = cv2.imencode('.png', img )[1].tobytes()
    return Response( img_bytes , media_type='image/png' )
