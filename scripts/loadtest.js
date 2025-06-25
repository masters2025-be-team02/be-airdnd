import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

export let options = {
    vus: 100,
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<500'],
    }
};

let timeoutErrors = new Counter('timeout_errors');
let otherErrors = new Counter('other_errors');
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
            if (r.status === 0 && r.error === 'context deadline exceeded') {
                timeoutErrors.add(1);
            } else if (![200, 409, 410].includes(r.status)) {
                otherErrors.add(1);
            }
            return [200, 409, 410].includes(r.status);
        }
    });

    sleep(1);
}
