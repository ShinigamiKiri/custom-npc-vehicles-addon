import urllib.request, json, time

req = urllib.request.Request('https://api.github.com/repos/ShinigamiKiri/custom-npc-vehicles-addon/actions/runs?per_page=1')
req.add_header('User-Agent', 'Mozilla/5.0')

while True:
    response = urllib.request.urlopen(req)
    data = json.loads(response.read().decode())
    run = data['workflow_runs'][0]
    status = run.get('status')
    conclusion = run.get('conclusion')
    print(f'Status: {status}, Conclusion: {conclusion}')
    if status == 'completed':
        break
    time.sleep(10)
