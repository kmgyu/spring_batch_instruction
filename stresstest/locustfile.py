# locustfile.py
# gpt 보일러 템플릿 코드
# Locust 2.x 이상에서 사용 가능
# 실행 예: locust -f locustfile.py --users 500 --spawn-rate 50 --host=http://localhost:8080

from locust import HttpUser, task, between, events
import random
import time

# ---------------------------
# CONFIG — 실제 환경에 맞게 수정 필요
# ---------------------------
TOTAL_POSTS = 10_000                   # 총 계정 수 (user1 .. user500)
DASHBOARD_PATH = "/board/"                # 자주 호출할 URL, board 확인하는 곳
# ---------------------------


class WebsiteUser(HttpUser):
    """
    하나의 Locust user는 시작 시 계정 하나를 가져와 로그인하고,
    이후 대시보드/새로고침 등의 요청을 반복합니다.
    """
    wait_time = between(0.5, 3)  # 요청 사이 랜덤 대기(초) — 실제 시나리오에 맞게 조정

    @task(5)
    def refresh_dashboard(self):
        """
        새로고침/대시보드 트래픽을 유발 — 빈도는 decorator의 인자로 조절
        """
        
        param = str(random.randint(1, TOTAL_POSTS))
        with self.client.get(DASHBOARD_PATH+param, name="GET board", catch_response=True) as r:
            if r.status_code == 200:
                r.success()
            else:
                r.failure(f"get board failed: {r.status_code}")



# Optional: 기록용 이벤트 훅 (테스트가 종료될 때 콘솔에 요약 출력)
@events.test_stop.add_listener
def on_test_stop(environment, **kwargs):
    print("=== Locust test stopped ===")
