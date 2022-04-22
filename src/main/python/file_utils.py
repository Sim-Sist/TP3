import os
import pathlib

SOURCE_FOLDER_NAME = "src"


def get_src():
    file_location = pathlib.Path(__file__).parent.resolve()
    current_location = file_location
    while current_location.name != SOURCE_FOLDER_NAME:
        current_location = current_location.parent.resolve()
    return current_location.resolve().as_uri().replace("file://", "") + "/"


def move_to_src():
    src = get_src()
    os.chdir(src)
