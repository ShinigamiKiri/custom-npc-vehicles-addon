import urllib.request, json

repo = 'ShinigamiKiri/custom-npc-vehicles-addon'
runs_url = f'https://api.github.com/repos/{repo}/actions/runs?per_page=1'

req = urllib.request.Request(runs_url)
req.add_header('User-Agent', 'Mozilla/5.0')
with urllib.request.urlopen(req) as response:
    data = json.loads(response.read().decode())
    run = data['workflow_runs'][0]
    jobs_url = run['jobs_url']
    
    req2 = urllib.request.Request(jobs_url)
    req2.add_header('User-Agent', 'Mozilla/5.0')
    with urllib.request.urlopen(req2) as response2:
        jobs_data = json.loads(response2.read().decode())
        for job in jobs_data['jobs']:
            check_run_url = job.get('check_run_url')
            if check_run_url:
                req_anno = urllib.request.Request(check_run_url + '/annotations')
                req_anno.add_header('User-Agent', 'Mozilla/5.0')
                try:
                    annotations = json.loads(urllib.request.urlopen(req_anno).read().decode())
                    for a in annotations:
                        print(f"{a.get('path')}:{a.get('start_line')} - {a.get('message')}")
                except Exception as e:
                    print("Could not get annotations:", e)
