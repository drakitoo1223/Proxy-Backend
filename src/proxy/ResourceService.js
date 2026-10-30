export class RealResourceService {
  constructor(dataSource) {
    this.dataSource = dataSource;
  }

  getAllResources() {
    return this.dataSource.map(resource => ({ ...resource }));
  }

  getResourceById(id) {
    const resource = this.dataSource.find(item => item.id === Number(id));
    if (!resource) {
      return null;
    }

    return { ...resource };
  }
}

export class ProtectedResourceProxy {
  constructor(realService, userContext) {
    this.realService = realService;
    this.userContext = userContext;
    this.cache = new Map();
  }

  isAuthorized() {
    const allowedRoles = ['admin', 'member'];
    return Boolean(this.userContext) && allowedRoles.includes(this.userContext.role);
  }

  getAllResources() {
    if (!this.isAuthorized()) {
      throw new Error('Access denied: you do not have permission to see this data.');
    }

    const cacheKey = 'all-resources';
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey);
    }

    const result = this.realService.getAllResources();
    this.cache.set(cacheKey, result);
    return result;
  }

  getResourceById(id) {
    if (!this.isAuthorized()) {
      throw new Error('Access denied: you do not have permission to see this data.');
    }

    if (this.cache.has(id)) {
      return this.cache.get(id);
    }

    const result = this.realService.getResourceById(id);
    if (result) {
      this.cache.set(id, result);
    }

    return result;
  }
}
