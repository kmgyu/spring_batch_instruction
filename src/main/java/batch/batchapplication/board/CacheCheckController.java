package batch.batchapplication.board;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/cache")
@Controller
@RequiredArgsConstructor
public class CacheCheckController {
//  private final CacheManager cacheManager;
//
//  @GetMapping("/check")
//  public ResponseEntity<?> printCacheType() {
//    return ResponseEntity.ok("Current CacheManager: " + cacheManager.getClass().getName());
//  }
}
