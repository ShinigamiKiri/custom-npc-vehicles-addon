import urllib.request, json
req = urllib.request.Request('https://api.github.com/repos/ShinigamiKiri/custom-npc-vehicles-addon/actions/runs/32462228937/jobs')
req.add_header('User-Agent', 'Mozilla/5.0')
jobs = json.loads(urllib.request.urlopen(req).read().decode())['jobs']
for job in jobs:
    check_run_url = job.get('check_run_url')
    if check_run_url:
        req_anno = urllib.request.Request(check_run_url + '/annotations')
        req_anno.add_header('User-Agent', 'Mozilla/5.0')
        annotations = json.loads(urllib.request.urlopen(req_anno).read().decode())
        for a in annotations:
            print(f"{a.get('path')}:{a.get('start_line')} - {a.get('message')}")
