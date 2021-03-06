package com.example.study.openDartApi;

import com.example.study.openDartApi.dtoImpl.api.DartApiDetailDto;
import com.example.study.openDartApi.dtoImpl.api.DartApiResponseDto;
import com.example.study.openDartApi.dtoImpl.evaluate.OperatingIncomeGrowthRatioEvaluation;
import com.example.study.openDartApi.entity.stock.CorpDetail;
import com.example.study.openDartApi.entity.stock.Corporation;
import com.example.study.openDartApi.service.api.ApiService;
import com.example.study.openDartApi.service.evaluate.EvaluateService;
import com.example.study.openDartApi.service.evaluate.SortableService;
import com.example.study.openDartApi.service.keyCount.KeyService;
import com.example.study.openDartApi.utils.evaluate.CorpEvaluator;
import com.example.study.openDartApi.utils.parser.DartXmlParser;
import com.example.study.openDartApi.utils.stream.ZipStream;
import org.jdom2.JDOMException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import javax.naming.LimitExceededException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

@SpringBootTest
class OpenDartApiApplicationTests {
	private Environment environment;
	private ApiService dartZipService;
	private ApiService  dartService;
	private ApiService	dartJsonService;
	private KeyService dartKeyCountService;

	private EvaluateService evaluateService;

	@Autowired
	public OpenDartApiApplicationTests(
			  Environment 	environment
			, KeyService 	dartKeyCountService
			, @Qualifier("DartZipService")  ApiService dartZipService
			, @Qualifier("DartTestService") ApiService dartService
			, @Qualifier("DartJsonService") ApiService dartJsonService
			, @Qualifier("OperatingIncomeGrowthRatioEvaluationService") EvaluateService evaluateService
	) {
		this.environment        	= environment;
		this.dartKeyCountService	= dartKeyCountService;

		this.dartZipService 		= dartZipService;
		this.dartService        	= dartService;
		this.dartJsonService 		= dartJsonService;
		this.evaluateService		= evaluateService;
	}

	@Test
	public void getOperatingIncomeRatioTest() {
		List<Corporation> corporations = new ArrayList<>();

		/**
		 * corporation1 - corpEvals : null
		 * corporation2 - corpEvals : null
		 * corporation3 - corpEvals : null
		 */
		Corporation corporation1 = new Corporation();
		Corporation corporation2 = new Corporation();
		Corporation corporation3 = new Corporation();

		corporation1.setCorpName("TEST1");
		corporation2.setCorpName("TEST2");
		corporation3.setCorpName("TEST3");

		corporation1.setCorpEvals(new HashMap<>());
		corporation2.setCorpEvals(new HashMap<>());
		corporation3.setCorpEvals(new HashMap<>());

		/**
		 * corporation1 - corpEvals : null
		 * corporation2 - corpEvals : new CorpDetail
		 */
		OperatingIncomeGrowthRatioEvaluation operatingIncomeGrowthRatioEvaluation1 = new OperatingIncomeGrowthRatioEvaluation();
		operatingIncomeGrowthRatioEvaluation1.setEvalDone(true);

		OperatingIncomeGrowthRatioEvaluation operatingIncomeGrowthRatioEvaluation2 = new OperatingIncomeGrowthRatioEvaluation();
		operatingIncomeGrowthRatioEvaluation2.setEvalDone(true);

		OperatingIncomeGrowthRatioEvaluation operatingIncomeGrowthRatioEvaluation3 = new OperatingIncomeGrowthRatioEvaluation();
		operatingIncomeGrowthRatioEvaluation3.setEvalDone(true);
		operatingIncomeGrowthRatioEvaluation3.setOperatingIncomeGrowthRatio(10.0f);

		corporation1.getCorpEvals().put(evaluateService.getServiceName(), operatingIncomeGrowthRatioEvaluation3);
		corporation2.getCorpEvals().put(evaluateService.getServiceName(), operatingIncomeGrowthRatioEvaluation1);
		corporation3.getCorpEvals().put(evaluateService.getServiceName(), operatingIncomeGrowthRatioEvaluation2);

		corporations.add(corporation1);
		corporations.add(corporation2);
		corporations.add(corporation3);

		if (evaluateService instanceof SortableService) {
			Comparator comparator = ((SortableService) evaluateService).getComparator();

			Collections.sort(corporations, comparator);

			for (Corporation corporation : corporations) {
				System.out.println("corporation.getCorpName() 		= " + corporation.getCorpName());
				System.out.println("corporation.getCorpEvals() 		= " + corporation.getCorpEvals());
			}
		}
	}

	@Test
	public void getMaxCorpDetailLengthTest() throws LimitExceededException, InterruptedException, IOException, JDOMException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final String CORP_CODE_URI = environment.getProperty("dart.corpCode.uri");

		ResponseEntity<byte[]> response = dartZipService.get(
				CORP_CODE_URI + "?"
						+ "crtfc_key=" + dartKeyCountService.getKey()
				, new HttpHeaders()
		);

		List<String> corpKeysXml = ZipStream.streamZip(response.getBody(), "UTF-8");

		String[]                    tags        = {"corp_code", "corp_name"};
		List<Map<String, String>>   corpKeys    = DartXmlParser.parse(corpKeysXml.get(0), tags);

		System.out.println("corpKeys.size() = " + corpKeys.size());

		final String 					CORP_DETAIL_URI = environment.getProperty("dart.corpDetail.uri");
		final int                       JOIN_LENGTH 	= 850;
		Map<String, List<CorpDetail>>   output      	= new HashMap<>();

		int         targetYear  = LocalDate.now().getYear();
		String[]    reprtCodes  = {"11011", "11014", "11012", "11013"};
		int         storeCount  = 0;

		while(storeCount <= 4
			&& (LocalDate.now().getYear() - targetYear) < 5
		) {
			boolean         isStored    = false;
			for (String reprtCode : reprtCodes) {
				int         joinIdx     = 0;
				String      corpKeysStr = joinCorpKeys(corpKeys, joinIdx, joinIdx + JOIN_LENGTH);

				while(!"".equals(corpKeysStr)) {
					ResponseEntity<DartApiResponseDto> response2 = dartJsonService.get(
							CORP_DETAIL_URI + "?"
									+ "crtfc_key="  + dartKeyCountService.getKey() + "&"
									+ "corp_code="  + corpKeysStr   + "&"
									+ "bsns_year="  + targetYear    + "&"
									+ "reprt_code=" + reprtCode
							, new HttpHeaders()
							, DartApiResponseDto.class
					);

					System.out.println("response2.getStatusCodeValue() = " + response2.getStatusCodeValue());
					System.out.println("response2.getBody().getStatus() = " + response2.getBody().getStatus());
					System.out.println("response2.getBody().getMessage() = " + response2.getBody().getMessage());

					if ("000".equals(response2.getBody().getStatus())
							|| "013".equals(response2.getBody().getStatus())) {
						if ("000".equals(response2.getBody().getStatus())) {
							parseDetailDto(output, response2.getBody());

							System.out.println("output.size() = " + output.size());
							isStored = true;
						}

						joinIdx 	+= JOIN_LENGTH;
						corpKeysStr = joinCorpKeys(corpKeys, joinIdx, joinIdx + JOIN_LENGTH);
					} else {
						throw new IllegalAccessException();
					}
				}

				if (isStored) {
					storeCount++;
					break;
				}
			}
			targetYear--;
		}

		Collection<List<CorpDetail>> oneOutput = output.values();

		for (List<CorpDetail> oneOut : oneOutput) {
			for (CorpDetail corpDetail : oneOut) {
				System.out.println("corpDetail = " + corpDetail.toString());
			}
		}
	}

	/**
	 * getCorpKeysStr
	 *
	 * Open Dart API ???????????? ????????? ????????? ???????????? ??????????????? ',' ??? Separator ??? ?????? join ?????????.
	 *
	 * 2021.02.02
	 * ????????? ??????????????? corpKeys ??? join ?????? ????????? ??????
	 *
	 * @param corpInfos
	 * @param fromIdx
	 * @param toIdx
	 *
	 * @return ???????????? ?????????
	 */
	private String joinCorpKeys(final List<Map<String, String>> corpInfos, int fromIdx, int toIdx) {
		if (fromIdx < 0             || fromIdx >= corpInfos.size()
				|| fromIdx >= toIdx     || toIdx <= fromIdx
				|| toIdx < 0
		) {
			return "";
		} else if (toIdx >= corpInfos.size()) {
			toIdx = corpInfos.size() - 1;
		}

		StringBuilder stringBuilder = new StringBuilder();

		for (int idx = fromIdx ; idx < toIdx; idx++) {
			String corpInfo = corpInfos.get(idx).get("corp_code");

			stringBuilder.append(corpInfo);
			stringBuilder.append(',');
		}

		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		return stringBuilder.toString();
	}

	private final Map<String, String> DETAIL_MAPPER = new HashMap<String, String>() {{
		put("????????????" , "TotAssets");
		put("????????????" , "TotLiability");
		put("????????????" , "TotStockholdersEquity");
		put("?????????" , "StockholdersEquity");
		put("?????????" , "Revenue");
		put("????????????" , "OperatingIncome");
		put("?????????????????? ?????????" , "IncomeBeforeTax");
		put("???????????????" , "NetIncome");
	}};

	/**
	 * parseDetailDto
	 *
	 * DartResponseDto ?????? ?????? DartDetailDto ?????? ???????????? CorpDetail ???????????? ???????????????.
	 *
	 * @param input
	 * @param dartApiResponseDto
	 *
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	private void parseDetailDto(Map<String, List<CorpDetail>> input, final DartApiResponseDto dartApiResponseDto) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		List<DartApiDetailDto> dartApiDetailDtos = dartApiResponseDto.getList();

		for (DartApiDetailDto dartApiDetailDto : dartApiDetailDtos) {
			/**
			 * DartDetailDto ??? ?????????
			 * ??????????????? ????????????, ????????????, ????????????, ?????????, ?????????, ????????????, ?????????????????? ?????????, ?????????????????? ???????????? ?????????.
			 */
			if (
//                "CFS".equals(dartDetailDto.getFs_div()) &&
					DETAIL_MAPPER.containsKey(dartApiDetailDto.getAccount_nm())
			) {
				/**
				 * input ?????? corp_code, bsns_year, reprt_code ?????? ????????? CorpDetail ??? ???????????????.
				 * ????????? ?????? ???????????? ???????????????.
				 */
				List<CorpDetail>    corpDetails         = input.getOrDefault(dartApiDetailDto.getCorp_code(), null);
				CorpDetail          targetCorpDetail    = null;

				if (corpDetails == null) {
					corpDetails = new ArrayList<>();
					input.put(dartApiDetailDto.getCorp_code(), corpDetails);
				}

				for (CorpDetail corpDetail : corpDetails) {
					if (dartApiDetailDto.getBsns_year() == corpDetail.getBsnsYear()
							&& dartApiDetailDto.getReprt_code().equals(corpDetail.getReprtCode())
					) {
						targetCorpDetail = corpDetail;
						break;
					}
				}

				if (targetCorpDetail == null) {
					targetCorpDetail = new CorpDetail(
							dartApiDetailDto.getCorp_code()
							, dartApiDetailDto.getBsns_year()
							, dartApiDetailDto.getReprt_code()
							, dartApiDetailDto.getThstrm_dt()
							, dartApiDetailDto.getRcept_no());

					corpDetails.add(targetCorpDetail);
				}

				/**
				 * ?????? ????????? ?????? ???????????????.
				 *
				 * ?????????????????? ?????? ????????? ??????, ?????? ????????????????????? ????????? ???????????? ?????? ???????????????.
				 */
				Class   targetClass     = targetCorpDetail.getClass();
				Method targetGetMethod = null;
				Method  targetSetMethod = null;

				for (Method method : targetClass.getDeclaredMethods()) {
					if (method.getName().equals("set" + DETAIL_MAPPER.get(dartApiDetailDto.getAccount_nm()))) {
						targetGetMethod = targetClass.getMethod("get" + DETAIL_MAPPER.get(dartApiDetailDto.getAccount_nm()));
						targetSetMethod = method;
						break;
					}
				}

				if (targetSetMethod == null || targetGetMethod == null) {
					throw new NoSuchMethodException();
				}

				Object methodGetResult = targetGetMethod.invoke(targetCorpDetail);
				if (methodGetResult == null || "0".equals(methodGetResult.toString())) {
					targetSetMethod.invoke(targetCorpDetail, dartApiDetailDto.getThstrm_amount());
				}
			}
		}
	}

	@Test
	public void corpInfosTest() throws LimitExceededException, InterruptedException {
		/**
		 * ???????????? - 00126380
		 * ???????????? - 00421045
		 */
		ResponseEntity<Corporation> response = dartJsonService.get(
				"https://opendart.fss.or.kr/api/company.json" + "?"
						+ "crtfc_key=" + dartKeyCountService.getKey() + "&"
						+ "corp_code=" + "00126380"
				, new HttpHeaders()
				, Corporation.class
		);

		System.out.println("response = " + response.getStatusCode());
		System.out.println("response = " + response.getBody().toString());
	}

	@Test
    public void corpEvaluatorTest() {
	    String corpCode             = "00000000";
	    char corpCls                = 'K';
	    String[] totEquities        = {"50000"};
        String[] incomeBeforeTaxes  = {"-25000"};

        boolean result = CorpEvaluator.isLossBeforeTax(corpCode, corpCls, totEquities, incomeBeforeTaxes);
    }

//	/**
//	 * Open Dart API ???????????? ?????? ????????? -> ZIP ?????? Utils : utils ??? ??????
//	 *
//	 * ???????????? ??????
//	 *
//	 * {CORPDART.xml : ????????????.toString()}
//	 */
//	@Test
//	public Map<String, InputStream> getCorpKeysTest() {
//		String corpKeyUri   = environment.getProperty("dart.corpKey.uri");
//		String key          = environment.getProperty("dart.key");
//
//		ResponseEntity<byte[]> response    = corpKeysService.get(
//				corpKeyUri
//						+ "?crtfc_key=" + key
//				, new HttpHeaders());
//		Map<String, InputStream>     results     = ZipStream.getZipStream(response.getBody(), "UTF-8");
//
//        System.out.println("results = " + results);
//
//		return results;
//	}
//
//	/**
//	 * for testing
//	 *
//	 * 00434456 - ????????????
//	 * 00430964 - ???????????????
//	 * 00432403 - ????????????
//	 *
//	 * Open Dart API ???????????? ?????????
//	 *
//	 * {
//	 * status	        ?????? ??? ?????? ??????		(???????????? ?????? ??????)
//	 * message	        ?????? ??? ?????? ?????????		(???????????? ?????? ??????)
//	 * corp_name	    ????????????		??????????????????
//	 * corp_name_eng	????????????		????????????????????????
//	 * stock_name	    ?????????(?????????) ?????? ????????????(????????????)		?????????(?????????) ?????? ????????????(????????????)
//	 * stock_code	    ??????????????? ?????? ????????? ????????????		??????????????? ????????????(6??????)
//	 * ceo_nm	        ????????????		????????????
//	 * corp_cls	        ????????????		???????????? : Y(??????), K(?????????), N(?????????), E(??????)
//	 * jurir_no	        ??????????????????		??????????????????
//	 * bizr_no	        ?????????????????????		?????????????????????
//	 * adres	        ??????		??????
//	 * hm_url	        ????????????		????????????
//	 * ir_url	        IR????????????		IR????????????
//	 * phn_no	        ????????????		????????????
//	 * fax_no	        ????????????		????????????
//	 * induty_code	    ????????????		????????????
//	 * est_dt	        ?????????(YYYYMMDD)		?????????(YYYYMMDD)
//	 * acc_mt	        ?????????(MM)		?????????(MM)
//	 * }
//	 */
//	@Test
//	public String getCompInfo() {
//		String compInfoUri  = environment.getProperty("dart.compInfo.uri");
//		String key          = environment.getProperty("dart.key");
//		String compKey      = "00126380";
//
//		ResponseEntity<String> response = dartService.get(
//				compInfoUri + "?"
//						+ "crtfc_key=" + key + "&"
//						+ "corp_code=" + compKey
//				, new HttpHeaders()
//		);
//
//		System.out.println("response = " + response.getBody());
//
//		return response.getBody();
//	}
//
//	/**
//	 * Open Dart API ???????????? ????????? ?????????
//	 *
//	 */
//	@Test
//	public String getCompRpts() {
//		String compRptUri   = environment.getProperty("dart.compRpt.uri");
//		String key          = environment.getProperty("dart.key");
//		String corpCode     = "00126380";
//		String bsnsYear     = "2019";
//		String rptCode      = "11011";
//
//		ResponseEntity<String> response = dartService.get(
//				compRptUri + "?"
//						+ "crtfc_key="  + key       + "&"
//						+ "corp_code="  + corpCode  + "&"
//						+ "bsns_year="  + bsnsYear  + "&"
//						+ "reprt_code=" + rptCode
//				, new HttpHeaders()
//		);
//
//		System.out.println("response = " + response.getBody());
//
//		return null;
//	}
//
//	/**
//	 * Open Dart API ???????????? ???????????? ?????????
//	 *
//	 * ?????? ?????????..
//	 */
//	@Test
//	public Map<String, InputStream> getXBRL() {
//		String xbrlUri      = environment.getProperty("dart.xbrl.uri");
//		String key          = environment.getProperty("dart.key");
//		String rceptNo      = "20190401004781";
//		String reprtCode    = "11011";
//
//		ResponseEntity<byte[]>  response    = corpKeysService.get(
//				xbrlUri
//						+ "?crtfc_key=" + key       + "&"
//						+ "rcept_no="   + rceptNo   + "&"
//						+ "reprt_code=" + reprtCode
//				, new HttpHeaders());
//		Map<String, InputStream>     results     = ZipStream.getZipStream(response.getBody(), "UTF-8");
//
//		System.out.println("results = " + results);
//
//		return results;
//	}
//
//
//	/**
//	 * ???????????? ???????????? ?????????
//	 *
//	 * 00118008 - ????????????
//	 * 20201116001231 - ???????????????(2020.09)
//	 *
//	 * ????????? ???????? -> UTF-8 ????????? ????????? ?????? ??????
//	 */
//	@Test
//	public Map<String, InputStream> getDocument() {
//		String documentUri  = environment.getProperty("dart.document.uri");
//		String key          = environment.getProperty("dart.key");
//
//		ResponseEntity<byte[]>  response    = corpKeysService.get(
//				documentUri + "?"
//						+ "crtfc_key=" + key + "&"
//						+ "rcept_no=" + "20201116001231"
//				, new HttpHeaders());
//		Map<String, InputStream>     results     = ZipStream.getZipStream(response.getBody(), "UTF-8");
//		return results;
//	}
}
