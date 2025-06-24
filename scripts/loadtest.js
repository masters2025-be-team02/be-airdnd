import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 1000,
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<500'],
    }
};

export default function () {
    const memberId = Math.floor(Math.random() * 100000) + 1;

    const payload = JSON.stringify({ memberId: memberId });

    const res = http.post(
        'http://localhost:8080/api/event/1',
        payload,
        {
            headers: {
                'Content-Type': 'application/json',
            }
        }
    );

    check(res, {
        '응답 코드': (r) => {
            if (![200, 409, 410].includes(r.status)) {
                console.log(`❌ status=${r.status}, body=${r.body}`);
                return false;
            }
            return true;
        }
    });

    sleep(1);
}
