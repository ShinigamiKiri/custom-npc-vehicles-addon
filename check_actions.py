import urllib.request, json
req = urllib.request.Request('https://api.github.com/repos/ShinigamiKiri/custom-npc-vehicles-addon/actions/runs?per_page=1')
req.add_header('User-Agent', 'Mozilla/5.0')
with urllib.request.urlopen(req) as response:
    data = json.loads(response.read().decode())
    runs = data.get('workflow_runs', [])
    if runs:
        run = runs[0]
        print(f'Status: {run.get("status")}, Conclusion: {run.get("conclusion")}')
    else:
        print('No runs found.')
