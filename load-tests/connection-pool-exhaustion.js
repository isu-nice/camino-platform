import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 20,
    duration: '10s',
};

export default function () {
    const res = http.get('http://localhost:8081/api/albergues');

    console.log(`status: ${res.status}`);

    check(res, {
        '200 응답': (r) => r.status === 200,
    });
}