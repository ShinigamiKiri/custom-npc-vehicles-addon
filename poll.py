import urllib.request, json, time, sys

repo = 'ShinigamiKiri/custom-npc-vehicles-addon'
runs_url = f'https://api.github.com/repos/{repo}/actions/runs?per_page=1'

def check():
    req = urllib.request.Request(runs_url)
    req.add_header('User-Agent', 'Mozilla/5.0')
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode())
        if not data.get('workflow_runs'):
            return None
        return data['workflow_runs'][0]

print("Polling GitHub Actions...")
while True:
    run = check()
    if not run:
        print("No runs found.")
        sys.exit(1)
    status = run.get('status')
    conclusion = run.get('conclusion')
    print(f"Status: {status}, Conclusion: {conclusion}")
    if status == 'completed':
        if conclusion != 'success':
            print("Build failed!")
            sys.exit(1)
        break
    time.sleep(10)

artifacts_url = run.get('artifacts_url')
req = urllib.request.Request(artifacts_url)
req.add_header('User-Agent', 'Mozilla/5.0')
with urllib.request.urlopen(req) as response:
    data = json.loads(response.read().decode())
    print("\nArtifact JSON:")
    print(json.dumps(data, indent=2))
