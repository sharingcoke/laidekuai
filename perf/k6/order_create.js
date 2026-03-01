import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: Number(__ENV.VUS || 10),
  duration: __ENV.DURATION || "60s",
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<800"],
    checks: ["rate>0.95"],
  },
};

const apiBase = __ENV.BASE_URL || "http://127.0.0.1:9090/api";
const username = __ENV.LOGIN_USERNAME || "user1";
const password = __ENV.LOGIN_PASSWORD || "123456";
const addressId = Number(__ENV.ADDRESS_ID || 1);
const goodsId = Number(__ENV.GOODS_ID || 1);
const qty = Number(__ENV.QTY || 1);

function parseCode(response) {
  try {
    return JSON.parse(response.body).code;
  } catch (e) {
    return null;
  }
}

export default function () {
  const loginRes = http.post(
    `${apiBase}/auth/login`,
    JSON.stringify({ username, password }),
    { headers: { "Content-Type": "application/json" } }
  );

  check(loginRes, {
    "login status 200": (r) => r.status === 200,
    "login business code 0": (r) => parseCode(r) === 0,
  });

  let token = "";
  try {
    token = JSON.parse(loginRes.body).data.access_token;
  } catch (e) {
    token = "";
  }
  if (!token) {
    sleep(1);
    return;
  }

  const orderRes = http.post(
    `${apiBase}/orders`,
    JSON.stringify({
      address_id: addressId,
      items: [{ goods_id: goodsId, qty }],
    }),
    {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    }
  );

  check(orderRes, {
    "order create status 200": (r) => r.status === 200,
    "order create business code 0": (r) => parseCode(r) === 0,
  });

  sleep(1);
}
