import numpy as np
import cv2

# uvicorn main:app --host 192.168.43.154 --port 8080 --reload

# Returns the coordinates ( x , y , w , h ) for the bounding box of the document in the given
# image. This image processing algorithm has a number of limitations like:
# 1. It requires high contrast between the document and the background to detect the edges
# 2. The document should be contained within the image only ( the four vertices of the document must be present
#    in the input image )
def get_rect( img ):

    # Erosion of the image - Unwanted pixel noise is eliminated by eroding the image,
    # thereby enhancing the document-background borders.
    kernel = np.ones( (7, 7), np.uint8)
    img = cv2.erode( img , kernel , iterations=1 )

    # Colorspace conversion - For our use-case, we don't require any color information as such ( this leads us to
    # first limitation though ). It is also necessary as we're performing contour detection in further steps.
    img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    #
    img = cv2.GaussianBlur(img, (9, 9), 0)
    img = cv2.Canny(img, 75, 200)
    contours, _ = cv2.findContours(img, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    contour_perimeters = [cv2.arcLength(contour, True) for contour in contours]
    doc_contour = contours[np.argmax(contour_perimeters)]
    x, y, w, h = cv2.boundingRect(doc_contour)
    return x, y, w, h

def get_rect_2( img ):
    # ---
    img = cv2.cvtColor( img , cv2.COLOR_BGR2GRAY)
    img = cv2.adaptiveThreshold( img, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 25, 5)

    kernel = np.ones((5, 5), np.uint8)
    img = cv2.morphologyEx(img, cv2.MORPH_CLOSE, kernel)

    kernel = np.ones((11, 11), np.uint8)
    img = cv2.erode(img, kernel, iterations=1)

    img = cv2.Canny(img, 75, 200)
    contours, _ = cv2.findContours(img, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    contour_perimeters = [cv2.arcLength(contour, True) for contour in contours]
    doc_contour = contours[np.argmax(contour_perimeters)]
    assert doc_contour is not None
    x, y, w, h = cv2.boundingRect(doc_contour)
    return x , y , w , h

def get_binarized_img( img ):
    img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    return cv2.threshold( img , 150 , 255 , cv2.THRESH_BINARY )[ 1 ]

