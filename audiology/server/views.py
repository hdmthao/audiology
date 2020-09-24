from django.shortcuts import render
from django.http import HttpResponse
import json

# Create your views here.
def generate(request):
    if request.method == 'GET':
        emotion = {}
        fav = request.GET['favorite'].split(',')
        emotion["vui"] = float(request.GET['happiness'])
        emotion["buon"] = float(request.GET['sadness'])
        emotion["neutral"] = float(request.GET['neutral'])
        emotion["anger"] = float(request.GET["anger"])
        emotion["fear"] = float(request.GET["fear"])

        emo = max(emotion, key=emotion.get)

        if emo in ["vui", "buon"]:
            response = json.dumps([{'query': 'Nhac ' + emo}])
        elif emo  == "neutral":
            query = "nhac hot "
            prefix = ""
            for f in fav:
                query += prefix
                prefix = " OR "
                query += f
            response = json.dumps([{'query': 'Nhac ' + query}])
        else:
            response = json.dumps([{'query': 'Nhac tinh tam'}])

    else:
        response = json.dumps([{'status': 'fail'}])
    
    return HttpResponse(response, content_type='text/json')

    pass