/**
 * Carga opcional (RAMAS / Pipe). Requiere app + PostgreSQL en marcha:
 *   docker compose up -d && ./mvnw spring-boot:run
 *   k6 run carga/pedidos.js
 */
import http from 'k6/http';
import { check } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 20,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate==0'],
    http_req_duration: ['p(95)<2000'],
  },
};

export default function () {
  const key = `k6-${__VU}-${__ITER}-${Date.now()}`;
  const res = http.post(
    `${baseUrl}/api/pedidos`,
    JSON.stringify({
      restauranteId: 1,
      clienteId: 10,
      items: [{ itemId: 1, cantidad: 1 }],
    }),
    {
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': key,
      },
    },
  );
  check(res, { 'pedido creado': (r) => r.status === 201 });
}
