import streamlit as st
import numpy as np
import cv2
from PIL import Image
import io
from document import get_rect_2

st.title( 'Document Scanning with OpenCV' )
file = st.file_uploader( 'Upload an image ...' )
if file is not None:
    img = Image.open( io.BytesIO( file.getvalue() ) )
    img = np.asarray( img )
    x , y , w , h = get_rect_2( img )
    cv2.rectangle( img , pt1=( x , y ), pt2=( x + w , y + h ), color=(255,0,0), thickness=20)
    st.image( img )


