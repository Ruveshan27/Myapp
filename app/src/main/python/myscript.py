from pytube import Playlist
import requests
from bs4 import BeautifulSoup
def main(Link):
    playlist = Playlist(Link)
    return playlist.video_urls

def isLoadshedding():
    URL = "https://loadshedding.eskom.co.za/LoadShedding/Index"
    page = requests.get(URL)

    soup = BeautifulSoup(page.content, "html.parser")

    for points in soup.find_all('span')[4]:
        point = str(points)
    return point

def countVids(Link):
    playlist = Playlist(Link)
    return len(playlist.video_urls)



        
   