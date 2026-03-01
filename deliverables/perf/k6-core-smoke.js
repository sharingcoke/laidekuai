import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    public_goods_success: {
      executor: 'constant-vus',
      vus: 2,
      duration: '10s',
      exec: 'publicGoodsSuccess',
    },
    admin_unauthorized_fail: {
      executor: 'constant-vus',
      vus: 1,
      duration: '10s',
      exec: 'adminUnauthorizedFail',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.20'],
    checks: ['rate>0.95'],
    'http_req_duration{scenario:public_goods_success}': ['p(95)<800'],
  },
};

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:9090/api';

export function publicGoodsSuccess() {
  const res = http.get(`${baseUrl}/goods?page=1&size=10`);
  check(res, {
    'goods list status is 200': (r) => r.status === 200,
  });
}

export function adminUnauthorizedFail() {
  const res = http.get(`${baseUrl}/admin/disputes?page=1&size=10`);
  check(res, {
    'admin endpoint rejects anonymous': (r) => r.status === 401 || r.status === 403,
  });
}
