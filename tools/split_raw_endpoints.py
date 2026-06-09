#!/usr/bin/env python3
import json
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
raw_dir = root / 'app' / 'src' / 'main' / 'res' / 'raw'

if not raw_dir.exists():
    print('raw dir not found:', raw_dir)
    raise SystemExit(1)

def sanitize(name: str) -> str:
    # keep lowercase alnum and underscores
    name = name.lower()
    name = re.sub(r'[^a-z0-9_]', '_', name)
    name = re.sub(r'_+', '_', name)
    return name.strip('_')

created = []
for path in raw_dir.glob('*.json'):
    with path.open('r', encoding='utf-8') as f:
        try:
            data = json.load(f)
        except Exception as e:
            print(f'Failed to parse {path}:', e)
            continue
    if not isinstance(data, dict):
        print('Skipping non-object json:', path)
        continue
    endpoints = data.get('endpoints')
    if not isinstance(endpoints, dict):
        print('No "endpoints" object in', path)
        continue
    src = path.stem
    for key, value in endpoints.items():
        out_name = f"{src}_{key}"
        out_name = sanitize(out_name) + '.json'
        out_path = raw_dir / out_name
        # wrap into same structure
        out_obj = { 'endpoints': { key: value } }
        with out_path.open('w', encoding='utf-8') as out_f:
            json.dump(out_obj, out_f, ensure_ascii=False, indent=2)
        created.append(str(out_path.relative_to(root)))

print('Created', len(created), 'files:')
for c in created:
    print(' -', c)

