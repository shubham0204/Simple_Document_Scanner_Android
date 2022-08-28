import cv2
import numpy as np


# uvicorn main:app --host 192.168.43.154 --port 8080 --reload

# Returns the coordinates ( x , y , w , h ) for the bounding box of the document in the given
# image. This image processing algorithm has a number of limitations like:
# 1. It requires high contrast between the document and the background to detect the edges
# 2. The document should be contained within the image only ( the four vertices of the document must be present
#    in the input image )
def get_rect( img ):

    # Colorspace conversion - For our use-case, we don't require any color information as such ( this leads us to
    # first limitation though ). It is also necessary as we're performing contour detection in further steps.
    img = cv2.cvtColor( img , cv2.COLOR_BGR2GRAY)

    # Thresholding with Otsu's Algorithm: Maximizes inter-class variance. Basically, it best splits the image into
    # foreground and background
    _ , img = cv2.threshold( img , 0 , 255 , cv2.THRESH_BINARY + cv2.THRESH_OTSU )

    # Performing morphological operations on the image: These operations work on the boundaries of the object (
    # or its shape/morphology )
    # First, we perform morphological closing to fill small holes produced after thresholding.
    # The image is first dilated and then eroded to perform this operation.
    kernel = np.ones((5, 5), np.uint8)
    img = cv2.morphologyEx(img, cv2.MORPH_CLOSE, kernel)

    # We perform additional erosion to remove finer details.
    # By performing all these morphological operations, we wish to preserve only the overall structure of the
    # document ( the rectangular structure of the document )
    # Also, we eliminate all inner details present inside the document, like text/images written in the document
    kernel = np.ones((11, 11), np.uint8)
    img = cv2.erode(img, kernel, iterations=1)

    # Image cleaning is done, we now perform Canny edge detection
    img = cv2.Canny(img, 75, 200)

    # Find contours within the edges
    contours, _ = cv2.findContours(img, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

    # Find the contour with the largest arc length
    contour_perimeters = [cv2.arcLength(contour, True) for contour in contours]
    doc_contour = contours[np.argmax(contour_perimeters)]

    assert doc_contour is not None

    # Compute a rect with the largest contour
    x, y, w, h = cv2.boundingRect(doc_contour)
    return x , y , w , h

# Binarize the image to give it a 'scanned' effect
def get_binarized_img( img ):
    img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    return cv2.adaptiveThreshold( img , 255 ,cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 35 , 10 )

