import urllib.request
import zipfile
import os

req1 = urllib.request.Request("https://fonts.google.com/download?family=Quicksand", headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req1) as response, open("q.zip", 'wb') as out_file:
    out_file.write(response.read())

req2 = urllib.request.Request("https://fonts.google.com/download?family=Plus%20Jakarta%20Sans", headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req2) as response, open("p.zip", 'wb') as out_file:
    out_file.write(response.read())

with zipfile.ZipFile("q.zip", 'r') as zip_ref:
    zip_ref.extractall("q_out")
with zipfile.ZipFile("p.zip", 'r') as zip_ref:
    zip_ref.extractall("p_out")
