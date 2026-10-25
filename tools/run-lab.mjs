import newman from 'newman';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const out = path.join(root, 'evidence');
fs.mkdirSync(out, { recursive: true });
const collection = JSON.parse(fs.readFileSync(path.join(root, 'postman/taskgotchi.postman_collection.json')));
const environment = JSON.parse(fs.readFileSync(path.join(root, 'postman/taskgotchi.postman_environment.json')));
newman.run({ collection, environment, reporters: ['cli', 'json'], reporter: { json: { export: path.join(out, 'newman-run.json') } } }, (error, result) => {
  if (error) throw error;
  const rows = result.run.executions.map(e => ({ name: e.item.name, method: e.request.method, status: e.response?.code, assertions: e.assertions, body: e.response?.stream?.toString() }));
  const summary = { timestamp: new Date().toISOString(), runner: 'Newman 6', stats: result.run.stats, failures: result.run.failures.map(f => ({ test: f.error.test, message: f.error.message })), cases: rows };
  fs.writeFileSync(path.join(out, 'lab-summary.json'), JSON.stringify(summary, null, 2));
  console.log('Evidencia: evidence/lab-summary.json');
  process.exitCode = result.run.failures.length ? 1 : 0;
});
