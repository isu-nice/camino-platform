import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 20,
    iterations: 20,
};

export default function () {
    const payload = JSON.stringify({
        albergueId: 1,
        pilgrimId: Math.floor(Math.random() * 10000),
        reservationDate: '2026-08-01',
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const res = http.post('http://localhost:8081/api/reservations', payload, params);

    console.log(`status: ${res.status}, body: ${res.body}`);

    check(res, {
        '정상 응답(200 또는 409)': (r) => r.status === 200 || r.status === 409,
    });
}