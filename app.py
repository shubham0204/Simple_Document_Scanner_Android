import io

import cv2
import numpy as np
import streamlit as st
from PIL import Image
from document import get_rect

# Streamlit application to test the document scanning algorithm
st.title( '📄 Document Scanning with OpenCV' )
st.markdown(
"""
Upload an image containing a document. Make sure that,
1. There's good contrast between the background and the document.
2. The entire document is contained within the image. Meaning, all corners of the document should be visible in the image
"""
)

file = st.file_uploader( 'Upload an image 📝...' )
if file is not None:
    img = Image.open( io.BytesIO( file.getvalue() ) )
    img = np.asarray( img )
    x , y , w , h = get_rect( img )
    cv2.rectangle( img , pt1=( x , y ), pt2=( x + w , y + h ), color=(255,0,0), thickness=20)
    st.image( img )


