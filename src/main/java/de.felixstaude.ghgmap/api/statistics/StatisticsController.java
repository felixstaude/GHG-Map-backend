package de.felixstaude.ghgmap.api.statistics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @GetMapping("/pins/today")
    public int getTodayPinCount() {
        return PinsPerDay.getPinsToday();
    }

    @GetMapping("/pins/day")
    public int getPinsForDay(@RequestParam("day") String day,
                             @RequestParam("month") String month,
                             @RequestParam("year") String year) {
        return PinsPerDay.getPinsForSpecificDay(year, month, day);
    }

    @GetMapping("/pins/month")
    public int getPinsForMonth(@RequestParam("month") String month,
                               @RequestParam("year") String year) {
        return PinsPerMonth.getPinsForSpecificMonth(year, month);
    }

    @GetMapping("/pins/year")
    public int getPinsForYear(@RequestParam("year") String year) {
        return PinsPerYear.getPinsForSpecificYear(year);
    }

    @GetMapping("/user/pins")
    public Map<String, Object> getUserPins(@RequestParam("userId") String userId) {
        return User.getPinsByUserId(userId);
    }
}
