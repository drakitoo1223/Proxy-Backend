import express from 'express';
import cors from 'cors';
import { RealResourceService, ProtectedResourceProxy } from './proxy/ResourceService.js';

const app = express();
const port = process.env.PORT || 4000;

const resources = [
  { id: 1, name: 'Secure Dashboard', category: 'analytics', access: 'member', visibility: 'internal' },
  { id: 2, name: 'Client Report', category: 'reporting', access: 'member', visibility: 'internal' },
  { id: 3, name: 'Public Knowledge Base', category: 'documentation', access: 'guest', visibility: 'public' },
  { id: 4, name: 'Billing Overview', category: 'finance', access: 'admin', visibility: 'private' }
];

app.use(cors());
app.use(express.json());

function resolveUser(req) {
  const role = req.headers['x-user-role'];

  if (role === 'admin' || role === 'member') {
    return { role };
  }

  return { role: 'guest' };
}

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', message: 'Proxy backend is running.' });
});

app.get('/api/resources', (req, res) => {
  try {
    const user = resolveUser(req);
    const proxy = new ProtectedResourceProxy(new RealResourceService(resources), user);
    const data = proxy.getAllResources();
    res.json({ user: user.role, resources: data });
  } catch (error) {
    res.status(403).json({ message: error.message });
  }
});

app.get('/api/resources/:id', (req, res) => {
  try {
    const user = resolveUser(req);
    const proxy = new ProtectedResourceProxy(new RealResourceService(resources), user);
    const resource = proxy.getResourceById(req.params.id);

    if (!resource) {
      return res.status(404).json({ message: 'Resource not found.' });
    }

    res.json({ user: user.role, resource });
  } catch (error) {
    res.status(403).json({ message: error.message });
  }
});

app.listen(port, () => {
  console.log(`Backend running on http://localhost:${port}`);
});
