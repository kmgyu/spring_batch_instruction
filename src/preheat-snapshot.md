기존 : UserDetails에 SecurityContext를 저장

이는 세션 저장소에 많은 정보를 저장해 비효율적이며, RCE 취약점을 노출할 수 있다.

따라서 UserSnapshot이라는 일종의 DTO를 제공해 정보량을 줄이고, @Class 정보를 제거한다.
또한 Redis에서 정보를 가져와 Deserialize하여 UserDetails를 생성해 세션 정보를 확인하는 과정을 거친다.

-> 오버헤드가 더 길어지는 것 아닌가? 생각할 수 있다.
그래서 그걸 확인해야 한다...!