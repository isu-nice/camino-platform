import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 30,
    duration: '15s',
};

export default function () {
    const res = http.get('http://localhost:8081/api/albergues/1');
    check(res, { '200 응답': (r) => r.status === 200 });
    sleep(0.1);
}