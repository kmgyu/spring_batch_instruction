# locustfile.py
# gpt 보일러 템플릿 코드
# Locust 2.x 이상에서 사용 가능
# 실행 예: locust -f locustfile.py --users 500 --spawn-rate 50 --host=http://localhost:8080

from locust import HttpUser, task, between, events
import queue
import random
import time
import re
from typing import Optional

# ---------------------------
# CONFIG — 실제 환경에 맞게 수정 필요
# ---------------------------
TOTAL_USERS = 10000                   # 총 계정 수 (user1 .. user500)
LOGIN_PATH = "/auth/login"          # 로그인 처리 URL (POST)
LOGIN_AS_JSON = False                # True면 JSON body로, False면 form data로 전송
USERNAME_FIELD = "username"         # 로그인 파라미터명: username/email 등
PASSWORD_FIELD = "password"
DASHBOARD_PATH = "/"                # 로그인 후 자주 호출할 URL 예시
REFRESH_PATH = "/"         # 새로고침용 엔드포인트 예시
USE_CSRF = False                    # True면 CSRF 토큰을 GET하여 전송 (보일러 템플릿 구현)
CSRF_FIELD = "csrfmiddlewaretoken"  # CSRF 폼 필드명 (Django 등)
# ---------------------------


# Pre-generate credential queue so each simulated user takes one account
_credentials_q: queue.Queue = queue.Queue()
for i in range(1, TOTAL_USERS + 1):
    uname = f"user{i}"
    pwd = str(i)
    _credentials_q.put((uname, pwd))

# If you want credentials to be reused when exhausted, set this flag.
# If False and queue is empty, user will attempt to construct user id from a random number.
REUSE_CREDENTIALS_IF_EXHAUSTED = True


def _maybe_get_csrf_token(html: str) -> Optional[str]:
    """
    간단한 CSRF 토큰 파서 (폼에 숨겨진 input 값 검색).
    복잡한 템플릿을 쓰면 필요에 따라 개선하세요.
    """
    # name="csrfmiddlewaretoken" value="TOKEN"
    m = re.search(r'name="{}"\s+value="([^"]+)"'.format(re.escape(CSRF_FIELD)), html)
    if m:
        return m.group(1)
    # 혹은 <input type="hidden" id="csrf" value="...">
    m2 = re.search(r'id="csrf"\s+value="([^"]+)"', html)
    if m2:
        return m2.group(1)
    return None


class WebsiteUser(HttpUser):
    """
    하나의 Locust user는 시작 시 계정 하나를 가져와 로그인하고,
    이후 대시보드/새로고침 등의 요청을 반복합니다.
    """
    wait_time = between(0.5, 3)  # 요청 사이 랜덤 대기(초) — 실제 시나리오에 맞게 조정
    credential = None

    def on_start(self):
        # 계정 할당
        try:
            self.credential = _credentials_q.get_nowait()
        except Exception:
            if REUSE_CREDENTIALS_IF_EXHAUSTED:
                # 재사용: 랜덤 계정 생성 (user1~TOTAL_USERS)
                n = random.randint(1, TOTAL_USERS)
                self.credential = (f"user{n}", str(n))
            else:
                # fallback: make an ephemeral id
                n = random.randint(1, TOTAL_USERS)
                self.credential = (f"user{n}", str(n))

        username, password = self.credential

        # 로그인 전, CSRF 토큰이 필요한 경우 로그인 페이지를 먼저 GET
        headers = {"Accept": "application/json, text/html, */*"}
        csrf_token = None
        if USE_CSRF:
            resp = self.client.get(LOGIN_PATH, name="GET login page", headers=headers, catch_response=True)
            if resp.status_code == 200:
                csrf_token = _maybe_get_csrf_token(resp.text)
                # 쿠키(세션)는 self.client가 자동으로 보관
            else:
                resp.failure(f"Failed to GET login page: {resp.status_code}")

        # 로그인 요청
        if LOGIN_AS_JSON:
            body = {USERNAME_FIELD: username, PASSWORD_FIELD: password}
            if csrf_token:
                body[CSRF_FIELD] = csrf_token
                # CSRF가 필요하고 서버가 토큰을 헤더로 기대한다면 아래처럼 전송하도록 변경하세요:
                # headers["X-CSRFToken"] = csrf_token
            with self.client.post(LOGIN_PATH, json=body, headers=headers, catch_response=True, name="POST login") as r:
                if r.status_code in (200, 302):
                    # 200 OK or redirect on success — 테스트 환경에 맞게 성공 코드 조건 조정
                    r.success()
                else:
                    r.failure(f"Login failed: {r.status_code} - {r.text[:200]}")
        else:
            # form-encoded
            data = {USERNAME_FIELD: username, PASSWORD_FIELD: password}
            if csrf_token:
                data[CSRF_FIELD] = csrf_token
            with self.client.post(LOGIN_PATH, data=data, headers={"Content-Type": "application/x-www-form-urlencoded"}, catch_response=True, name="POST login (form)") as r:
                if r.status_code in (200, 302):
                    r.success()
                else:
                    r.failure(f"Login failed: {r.status_code} - {r.text[:200]}")

        # optional: 방문/초기화 페이지 요청 (로그인 후 redirect를 따르지 않으면 수동으로)
        # self.client.get(DASHBOARD_PATH, name="GET dashboard (after login)")

    def on_stop(self):
        # 계정 반납(원하면)
        if REUSE_CREDENTIALS_IF_EXHAUSTED:
            # 반납하면 다른 사용자가 재사용 가능
            try:
                _credentials_q.put_nowait(self.credential)
            except Exception:
                pass

    @task(5)
    def refresh_dashboard(self):
        """
        새로고침/대시보드 트래픽을 유발 — 빈도는 decorator의 인자로 조절
        """
        # 리프레시 시 약간의 랜덤 파라미터를 붙여 캐시 적중률을 낮춤
        params = {"_": str(int(time.time() * 1000)), "r": str(random.randint(1, 1000))}
        with self.client.get(REFRESH_PATH, params=params, name="GET refresh", catch_response=True) as r:
            if r.status_code == 200:
                r.success()
            else:
                r.failure(f"refresh failed: {r.status_code}")

    # @task(2)
    # def view_dashboard_and_assets(self):
    #     """
    #     대시보드와 관련된 자원(이미지/스크립트)들을 다운로드하여 실제 브라우저 트래픽을 흉내냄
    #     """
    #     with self.client.get(DASHBOARD_PATH, name="GET dashboard", catch_response=True) as r:
    #         if r.status_code != 200:
    #             r.failure(f"dashboard failed: {r.status_code}")
    #     # 자원 호출(예시). 실제 앱에 맞춰 경로 수정하세요.
    #     # 자원을 랜덤으로 호출하여 트래픽 분산
    #     assets = ["/static/js/app.js", "/static/css/main.css", "/static/img/logo.png"]
    #     asset = random.choice(assets)
    #     self.client.get(asset, name=f"GET asset {asset}", catch_response=False)

    # @task(1)
    # def idle_action(self):
    #     """
    #     짧은 대기 또는 다른 사용자 행동 흉내 — DB 네트워크에 부담이 적은 요청 삽입 가능
    #     """
    #     # 예: 알림 확인
    #     self.client.get("/notifications", name="GET notifications", catch_response=False)
    #     # 인위적 짧은 대기
    #     time.sleep(random.uniform(0.1, 0.6))


# Optional: 기록용 이벤트 훅 (테스트가 종료될 때 콘솔에 요약 출력)
@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    print("=== Locust test stopped ===")
    print(f"Remaining credentials in queue: {_credentials_q.qsize()}")
