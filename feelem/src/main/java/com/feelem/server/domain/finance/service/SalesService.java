package com.feelem.server.domain.finance.service;

import com.feelem.server.domain.filter.entity.Filter;
import com.feelem.server.domain.filter.repository.FilterRepository;
import com.feelem.server.domain.finance.dto.SalesGraphResponse;
import com.feelem.server.domain.finance.dto.SalesListResponse;
import com.feelem.server.domain.finance.dto.SalesPeriod;
import com.feelem.server.domain.finance.dto.SalesSortType;
import com.feelem.server.domain.finance.dto.SalesTotalResponse;
import com.feelem.server.domain.finance.entity.FilterTransaction;
import com.feelem.server.domain.finance.repository.FilterTransactionRepository;
import com.feelem.server.domain.user.entity.User;
import com.feelem.server.domain.user.service.UserService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesService {
  private final UserService userService;
  private final FilterRepository filterRepository;
  private final FilterTransactionRepository filterTransactionRepository;

  /**
   * 1. 종합 판매 수치 조회 (정산금액, 총 판매량)
   */
  public SalesTotalResponse getTotalSales(SalesPeriod period) {
    User user = userService.getCurrentUser();
    LocalDateTime[] range = calculateDateRange(period);

    List<FilterTransaction> transactions = filterTransactionRepository.findAllBySellerIdAndDateRange(
        user.getId(), range[0], range[1]
    );

    long totalSalesCount = transactions.size();

    // 1. 판매된 총 포인트 합계 (예: 1,000 포인트)
    long totalSoldPoints = transactions.stream()
        .mapToLong(FilterTransaction::getAmount)
        .sum();

    // 2. 정산 금액 계산 (정책: 1포인트당 5원 정산)
    // 1,000 포인트 * 5원 = 5,000원 정산
    long settlementAmount = totalSoldPoints * 5;

    return SalesTotalResponse.builder()
        .totalSales(totalSalesCount)
        .settlementAmount(settlementAmount) // 단위: KRW (원)
        .build();
  }

  /**
   * 2. 판매중인 필터 목록 조회
   */
  public Page<SalesListResponse> getSalesFilterList(SalesSortType sortBy, Pageable pageable) {
    User user = userService.getCurrentUser();

    // 1. 정렬 기준 변환 (Enum -> Sort)
    Sort sort = switch (sortBy) {
      case RECENT -> Sort.by(Sort.Direction.DESC, "createdAt");
      case COUNT -> Sort.by(Sort.Direction.DESC, "purchaseCount");
      case AMOUNT -> Sort.by(Sort.Direction.DESC, "totalSalesAmount");
      case NAME -> Sort.by(Sort.Direction.ASC, "name");
    };

    // 2. 정렬 조건이 적용된 Pageable 생성
    Pageable sortedPageable = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sort
    );

    // 3. 판매 내역이 있고, 혹은 유료이거나, 삭제 여부 상관없이 조회하는 메서드 호출
    Page<Filter> filters = filterRepository.findSoldOrPaidFilters(
        user.getId(), sortedPageable
    );

    return filters.map(SalesListResponse::from);
  }

  /**
   * 3. 개별 필터 상세 수치 및 그래프 데이터 조회
   */
  public SalesGraphResponse getFilterSalesDetail(Long filterId, SalesPeriod period) {
    User user = userService.getCurrentUser();

    // 필터 조회 및 본인 확인
    Filter filter = filterRepository.findById(filterId)
        .orElseThrow(() -> new IllegalArgumentException("필터를 찾을 수 없습니다."));

    if (!filter.getCreator().getId().equals(user.getId())) {
      throw new IllegalArgumentException("본인의 필터만 조회할 수 있습니다.");
    }

    // 기간 설정
    LocalDateTime[] range = calculateDateRange(period);
    LocalDateTime startDate = range[0];
    LocalDateTime endDate = range[1];

    // 해당 필터의 기간 내 트랜잭션 조회
    List<FilterTransaction> transactions = filterTransactionRepository.findAllByFilterIdAndDateRange(
        filterId, startDate, endDate
    );

    // 기간 내 총합 계산
    //
    long totalPoints = transactions.stream().mapToLong(FilterTransaction::getAmount).sum();
    long totalCount = transactions.size();

    // 그래프 데이터 생성 (빈 날짜 0으로 채우기 포함)
    Map<String, Long> graphData = generateGraphData(transactions, period, startDate, endDate);

    return SalesGraphResponse.builder()
        .totalSalesPoints(totalPoints)
        .totalSalesCount(totalCount)
        .salesGraphData(graphData)
        .build();
  }

  /**
   * 기간(Enum)에 따른 StartDate, EndDate 계산
   * @return [startDate, endDate]
   */
  private LocalDateTime[] calculateDateRange(SalesPeriod period) {
    LocalDateTime end = LocalDateTime.now(); // 종료 시점은 현재
    LocalDateTime start;

    switch (period) {
      // 시작일의 '0시 0분 0초'로 설정하여 해당 일의 모든 매출 포함
      case WEEK -> start = end.minusWeeks(1).toLocalDate().atStartOfDay();
      case MONTH -> start = end.minusMonths(1).toLocalDate().atStartOfDay();
      case YEAR -> start = end.minusYears(1).toLocalDate().atStartOfDay();
      default -> start = end.minusWeeks(1).toLocalDate().atStartOfDay();
    }
    return new LocalDateTime[]{start, end};
  }

  /**
   * 그래프 데이터 생성 로직
   * 트랜잭션이 없는 날짜도 0으로 채워서 리턴함
   */
  private Map<String, Long> generateGraphData(List<FilterTransaction> transactions,
      SalesPeriod period,
      LocalDateTime start,
      LocalDateTime end) {
    // 1. 트랜잭션 데이터를 날짜별로 그룹핑 (Map<"YYYY-MM-DD", SumAmount>)
    // YEAR인 경우 "YYYY-MM", 나머지는 "MM-dd"
    String pattern = (period == SalesPeriod.YEAR) ? "yyyy-MM" : "MM-dd";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

    // 판매 금액이 아니라 판매 개수를 보내야 함
    Map<String, Long> groupedData = transactions.stream()
        .collect(Collectors.groupingBy(
            t -> t.getPurchasedAt().format(formatter),
            Collectors.counting()
        ));

    // 2. 시작일부터 종료일까지 루프를 돌며 Map 채우기 (LinkedHashMap으로 순서 보장)
    Map<String, Long> result = new LinkedHashMap<>();

    LocalDate currDate = start.toLocalDate();
    LocalDate endDate = end.toLocalDate();

    // [추가] YEAR 조회일 경우, 시작일을 해당 월의 1일로 보정 (데이터 누락/중복 방지)
    if (period == SalesPeriod.YEAR) {
      currDate = currDate.withDayOfMonth(1);
    }

    while (!currDate.isAfter(endDate)) {
      String key;
      if (period == SalesPeriod.YEAR) {
        // 월 단위 루프
        key = currDate.format(formatter);
        result.put(key, groupedData.getOrDefault(key, 0L));

        // 다음 달 1일로 이동
        currDate = currDate.plusMonths(1);
      } else {
        // 일 단위 루프
        key = currDate.format(formatter);
        result.put(key, groupedData.getOrDefault(key, 0L));
        currDate = currDate.plusDays(1);
      }
    }
    return result;
  }
}
