import newman from 'newman';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const out = path.join(root, 'evidence');
fs.mkdirSync(out, { recursive: true });
const collection = JSON.parse(fs.readFileSync(path.join(root, 'postman/taskgotchi.postman_collection.json')));
const environment = JSON.parse(fs.readFileSync(path.join(root, 'postman/taskgotchi.postman_environment.json')));
const observations = [];
let currentCase = '';
const primaryMethods = Object.fromEntries(collection.item.flatMap(folder => folder.item.map(item => [item.name, item.request.method])));
newman.run({ collection, environment, reporters: ['cli', 'json'], reporter: { json: { export: path.join(out, 'newman-run.json') } } }, (error, result) => {
  if (error) throw error;
  const rows = observations.filter(e => e.method === primaryMethods[e.name]);
  const summary = { timestamp: new Date().toISOString(), runner: 'Newman 6', stats: result.run.stats, failures: result.run.failures.map(f => ({ test: f.error.test, message: f.error.message })), cases: rows, requests: observations };
  fs.writeFileSync(path.join(out, 'lab-summary.json'), JSON.stringify(summary, null, 2));
  console.log('Evidencia: evidence/lab-summary.json');
  process.exitCode = result.run.failures.length ? 1 : 0;
}).on('beforeItem', (error, args) => { currentCase = args.item.name; })
  .on('request', (error, args) => {
    if (args.response) observations.push({ name: currentCase, method: args.request.method, status: args.response.code, body: args.response.stream.toString() });
  });
