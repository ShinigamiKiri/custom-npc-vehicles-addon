import urllib.request
import json
import zipfile
import io

url = "https://api.github.com/repos/ShinigamiKiri/custom-npc-vehicles-addon/actions/runs/32579118292/jobs"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as response:
    data = json.loads(response.read().decode())
    for job in data['jobs']:
        if job['conclusion'] == 'failure':
            print("Failed job ID:", job['id'])
            log_url = f"https://api.github.com/repos/ShinigamiKiri/custom-npc-vehicles-addon/actions/jobs/{job['id']}/logs"
            try:
                log_req = urllib.request.Request(log_url)
                with urllib.request.urlopen(log_req) as log_response:
                    logs = log_response.read().decode()
                    lines = logs.split('\n')
                    for i, line in enumerate(lines):
                        if "error:" in line or "FAILED" in line:
                            print('\n'.join(lines[max(0, i-5):min(len(lines), i+5)]))
                            break
            except Exception as e:
                print("Could not fetch logs:", e)
