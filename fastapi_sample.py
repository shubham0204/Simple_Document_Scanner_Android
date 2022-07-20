from fastapi import FastAPI , File , UploadFile
from fastapi.responses import FileResponse
import numpy as np
import cv2

app = FastAPI()

@app.get( "/sample/get_image" )
def get_image( image_name : str ):
    if image_name == 'kiara':
        return FileResponse( 'sample.png' , media_type='image/png' )

@app.post( "/sample/show_image" )
async def show_image( image : UploadFile = File() ):
    contents = await image.read()
    nparr = np.fromstring(contents, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    cv2.imwrite( 'img.png' , img )


