import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

export let options = {
    vus: 100,
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<1000'],
    }
};

let timeoutErrors = new Counter('timeout_errors');
let otherErrors = new Counter('other_errors');
export default function () {
    const memberId = Math.floor(Math.random() * 100000) + 1;

    const payload = JSON.stringify({ memberId: memberId });

    const res = http.post(
        'http://host.docker.internal:8080/api/event/1',
        payload,
        {
            headers: {
                'Content-Type': 'application/json',
            }
        }
    );

    check(res, {
        "status is 200": (r) => r.status === 200,
    }) || console.error(`Failed! status=${res.status}, body=${res.body}`);

    sleep(1);
}
