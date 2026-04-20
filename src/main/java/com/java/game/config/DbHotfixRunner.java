package com.java.game.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 기존 파일형 H2(DB 유지) 환경에서 컬럼 추가 누락으로 500이 나는 것 방지용 핫픽스.
 * (ddl-auto=update가 적용되기 전에/적용이 누락된 경우를 대비)
 */
@Component
@Order(0)
public class DbHotfixRunner implements CommandLineRunner {

	/** map 게시판 글 일괄 삭제 패치 (1회). 이전 패치 ID와 다르게 해야 이미 적용된 DB에서도 다시 실행됨 */
	private static final String PATCH_DELETE_MAP_BOARDS = "DELETE_MAP_BOARDS_20260406";

	private final DataSource dataSource;

	public DbHotfixRunner(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(String... args) throws Exception {
		try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
			// 최우선: 기존 H2에서 TRAINEE.GRADE가 ENUM(S~D)만 허용하면 N/R/SR/SSR 저장 시 실패함 → VARCHAR 전환
			try {
				s.execute("ALTER TABLE TRAINEE ALTER COLUMN GRADE SET DATA TYPE VARCHAR(10)");
			} catch (Exception ignored) {
			}

			// GAME_SCENE
			// game_scene(phase)에 UNIQUE 제약/인덱스가 걸려있으면 phase당 1건만 저장되어 "풀(다건)" 시딩이 실패한다.
			// H2 파일 DB가 유지되는 환경에서, 이름이 매번 달라질 수 있어 JDBC 메타데이터 기반으로 자동 제거한다.
			dropUniqueIndexOnSingleColumn(c, s, "PUBLIC", "GAME_SCENE", "PHASE");

			// GAME_RUN
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS PHASE VARCHAR(30) NOT NULL DEFAULT 'DAY1_MORNING'");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS CONFIRMED BOOLEAN NOT NULL DEFAULT FALSE");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS CURRENT_SCENE_ID BIGINT");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS MID_EVAL_TIER VARCHAR(2)");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS MID_EVAL_EFFECT_UNTIL_TURN INT");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS FATIGUE INT NOT NULL DEFAULT 0");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS REROLL_REMAINING INT NOT NULL DEFAULT 3");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS REROLL_LAST_AT TIMESTAMP");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS CORE_FANS INT DEFAULT 0");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS CASUAL_FANS INT DEFAULT 0");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS LIGHT_FANS INT DEFAULT 0");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS FAN_EVENT_FLAGS INT DEFAULT 0");
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS SCORE_CACHE INT");
			s.execute("UPDATE GAME_RUN SET REROLL_REMAINING=3 WHERE REROLL_REMAINING IS NULL");
			s.execute("UPDATE GAME_RUN SET REROLL_LAST_AT=CURRENT_TIMESTAMP WHERE REROLL_LAST_AT IS NULL");
			s.execute("UPDATE GAME_RUN SET CORE_FANS=0 WHERE CORE_FANS IS NULL");
			s.execute("UPDATE GAME_RUN SET CASUAL_FANS=0 WHERE CASUAL_FANS IS NULL");
			s.execute("UPDATE GAME_RUN SET LIGHT_FANS=0 WHERE LIGHT_FANS IS NULL");
			s.execute("UPDATE GAME_RUN SET FAN_EVENT_FLAGS=0 WHERE FAN_EVENT_FLAGS IS NULL");
			// LIGHT_FANS를 CASUAL_FANS(해외)로 합산 — 국내/해외 2축만 사용
			try {
				s.execute("UPDATE GAME_RUN SET CASUAL_FANS = COALESCE(CASUAL_FANS,0) + COALESCE(LIGHT_FANS,0)");
				s.execute("UPDATE GAME_RUN SET LIGHT_FANS = 0 WHERE LIGHT_FANS IS NOT NULL");
			} catch (Exception ignored) {
			}

			// BOARD (첨부/관리자 컬럼)
			s.execute("ALTER TABLE BOARD ADD COLUMN IF NOT EXISTS ORIGINAL_FILENAME VARCHAR(255)");
			s.execute("ALTER TABLE BOARD ADD COLUMN IF NOT EXISTS STORED_FILENAME VARCHAR(255)");
			s.execute("ALTER TABLE BOARD ADD COLUMN IF NOT EXISTS IS_IMAGE BOOLEAN NOT NULL DEFAULT FALSE");
			s.execute("ALTER TABLE BOARD ADD COLUMN IF NOT EXISTS AUTHOR_NICK VARCHAR(60)");
			s.execute("ALTER TABLE BOARD ADD COLUMN IF NOT EXISTS VIEW_COUNT BIGINT NOT NULL DEFAULT 0");
			s.execute("ALTER TABLE BOARD ADD COLUMN IF NOT EXISTS LIKE_COUNT BIGINT NOT NULL DEFAULT 0");
			s.execute("ALTER TABLE BOARD ADD COLUMN IF NOT EXISTS VISIBLE BOOLEAN NOT NULL DEFAULT TRUE");
			s.execute("ALTER TABLE BOARD ADD COLUMN IF NOT EXISTS POPUP BOOLEAN NOT NULL DEFAULT FALSE");
			s.execute("UPDATE BOARD SET IS_IMAGE=FALSE WHERE IS_IMAGE IS NULL");
			s.execute("UPDATE BOARD SET VIEW_COUNT=0 WHERE VIEW_COUNT IS NULL");
			s.execute("UPDATE BOARD SET LIKE_COUNT=0 WHERE LIKE_COUNT IS NULL");
			s.execute("UPDATE BOARD SET VISIBLE=TRUE WHERE VISIBLE IS NULL");
			s.execute("UPDATE BOARD SET POPUP=FALSE WHERE POPUP IS NULL");

			// MEMBER (Admin role)
			s.execute("ALTER TABLE MEMBER ADD COLUMN IF NOT EXISTS ROLE VARCHAR(20) NOT NULL DEFAULT 'USER'");
			s.execute("UPDATE MEMBER SET ROLE='USER' WHERE ROLE IS NULL OR TRIM(ROLE)=''");
			// MEMBER (Reroll charge: 1/hour, max 3) - account based
			s.execute("ALTER TABLE MEMBER ADD COLUMN IF NOT EXISTS REROLL_REMAINING INT NOT NULL DEFAULT 3");
			s.execute("ALTER TABLE MEMBER ADD COLUMN IF NOT EXISTS REROLL_LAST_AT TIMESTAMP");
			s.execute("UPDATE MEMBER SET REROLL_REMAINING=3 WHERE REROLL_REMAINING IS NULL");
			s.execute("UPDATE MEMBER SET REROLL_LAST_AT=CURRENT_TIMESTAMP WHERE REROLL_LAST_AT IS NULL");
			// MEMBER (회원 등급: 누적 rankExp + 등급 코드)
			s.execute("ALTER TABLE MEMBER ADD COLUMN IF NOT EXISTS RANK_EXP INT NOT NULL DEFAULT 0");
			s.execute("ALTER TABLE MEMBER ADD COLUMN IF NOT EXISTS MEMBER_RANK VARCHAR(32) NOT NULL DEFAULT 'ROOKIE'");
			s.execute("UPDATE MEMBER SET RANK_EXP=0 WHERE RANK_EXP IS NULL");
			s.execute("UPDATE MEMBER SET MEMBER_RANK='ROOKIE' WHERE MEMBER_RANK IS NULL OR TRIM(MEMBER_RANK)=''");
			// MEMBER (주민번호 암호화 저장 대응) - 기존 VARCHAR(20)인 DB를 확장
			try {
				s.execute("ALTER TABLE MEMBER ALTER COLUMN JUMIN SET DATA TYPE VARCHAR(255)");
			} catch (Exception ignored) {
			}

			// GAME_RUN — 플레이당 팬→경험치 1회 반영
			s.execute("ALTER TABLE GAME_RUN ADD COLUMN IF NOT EXISTS FAN_REWARD_APPLIED BOOLEAN NOT NULL DEFAULT FALSE");
			s.execute("UPDATE GAME_RUN SET FAN_REWARD_APPLIED=FALSE WHERE FAN_REWARD_APPLIED IS NULL");

			// FAN MAP (demo/portfolio) - 지역별 팬 분포 통계
			s.execute("CREATE TABLE IF NOT EXISTS FAN_MAP_STAT (" +
					"ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
					"SCOPE VARCHAR(10) NOT NULL," +
					"GU VARCHAR(40)," +
					"DONG VARCHAR(60)," +
					"LAT DOUBLE," +
					"LNG DOUBLE," +
					"STAT_DATE DATE NOT NULL," +
					"FAN_COUNT INT NOT NULL" +
					")");

			// TRAINEE (personality)
			s.execute("ALTER TABLE TRAINEE ADD COLUMN IF NOT EXISTS PERSONALITY_CODE VARCHAR(32)");
			s.execute("ALTER TABLE TRAINEE ADD COLUMN IF NOT EXISTS BIRTHDAY DATE");
			// 구 연습생 등급 문자열 → 마이그레이션 전 null 처리
			try {
				s.execute("UPDATE TRAINEE SET GRADE = NULL WHERE GRADE IN ('S','A','B','C','D')");
			} catch (Exception ignored) {
			}

			// CHAT_KEYWORD_RULE
			s.execute("CREATE TABLE IF NOT EXISTS CHAT_KEYWORD_RULE (" +
					"ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY," +
					"KEYWORD VARCHAR(40) NOT NULL," +
					"CHOICE_KEY VARCHAR(16) NOT NULL," +
					"PRIORITY INT NOT NULL," +
					"IS_ACTIVE BOOLEAN NOT NULL DEFAULT TRUE" +
					")");

			// TRAINEE_MEMBER_LIKE — 런당 1회 좋아요 (RUN_ID 추가, 기존 행은 RUN_ID=0)
			try {
				s.execute("ALTER TABLE TRAINEE_MEMBER_LIKE ADD COLUMN IF NOT EXISTS RUN_ID BIGINT");
				s.execute("UPDATE TRAINEE_MEMBER_LIKE SET RUN_ID = 0 WHERE RUN_ID IS NULL");
			} catch (Exception ignored) {
			}
			try {
				s.execute("ALTER TABLE TRAINEE_MEMBER_LIKE ALTER COLUMN RUN_ID SET NOT NULL");
			} catch (Exception ignored) {
			}
			try {
				s.execute("ALTER TABLE TRAINEE_MEMBER_LIKE DROP CONSTRAINT UK_TRAINEE_MEMBER_LIKE");
			} catch (Exception ignored) {
			}
			try {
				s.execute("ALTER TABLE TRAINEE_MEMBER_LIKE ADD CONSTRAINT UK_TRAINEE_MEMBER_LIKE_RUN UNIQUE (MEMBER_MNO, TRAINEE_ID, RUN_ID)");
			} catch (Exception ignored) {
			}

			// MY_TRAINEE (연습생 강화 단계)
			s.execute("ALTER TABLE MY_TRAINEE ADD COLUMN IF NOT EXISTS ENHANCE_LEVEL INT NOT NULL DEFAULT 0");
			s.execute("UPDATE MY_TRAINEE SET ENHANCE_LEVEL=0 WHERE ENHANCE_LEVEL IS NULL");

			// 길거리 캐스팅(map) 글만 DB에서 제거 (1회, 다른 게시판은 건드리지 않음)
			applyDeleteMapBoardsOnce(s);
		} catch (Exception ignored) {
			// 운영/과제 환경에서 스키마가 이미 최신이면 그냥 통과
		}
	}

	/**
	 * BOARD_TYPE = 'map' 인 글만 삭제 (신고·좋아요·댓글 정리 후 BOARD). {@link #PATCH_DELETE_MAP_BOARDS}당 1회만 실행.
	 */
	private void applyDeleteMapBoardsOnce(Statement s) throws Exception {
		s.execute("CREATE TABLE IF NOT EXISTS APP_SCHEMA_PATCH (" +
				"PATCH_ID VARCHAR(64) PRIMARY KEY," +
				"APPLIED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
				")");
		try (ResultSet rs = s.executeQuery(
				"SELECT COUNT(*) FROM APP_SCHEMA_PATCH WHERE PATCH_ID = '" + PATCH_DELETE_MAP_BOARDS + "'")) {
			if (rs.next() && rs.getLong(1) > 0) {
				return;
			}
		}

		try {
			s.execute("""
					DELETE FROM BOARD_REPORT WHERE TARGET_TYPE = 'comment' AND TARGET_ID IN (
					  SELECT ID FROM BOARD_COMMENT WHERE BOARD_ID IN (
					    SELECT ID FROM BOARD WHERE BOARD_TYPE = 'map'
					  )
					)""");
		} catch (Exception ignored) {
		}
		try {
			s.execute("""
					DELETE FROM BOARD_REPORT WHERE TARGET_TYPE = 'board' AND TARGET_ID IN (
					  SELECT ID FROM BOARD WHERE BOARD_TYPE = 'map'
					)""");
		} catch (Exception ignored) {
		}
		try {
			s.execute("""
					DELETE FROM BOARD_LIKE WHERE BOARD_ID IN (
					  SELECT ID FROM BOARD WHERE BOARD_TYPE = 'map'
					)""");
		} catch (Exception ignored) {
		}
		try {
			s.execute("DELETE FROM BOARD_COMMENT WHERE BOARD_ID IN (SELECT ID FROM BOARD WHERE BOARD_TYPE = 'map')");
		} catch (Exception ignored) {
		}
		s.execute("DELETE FROM BOARD WHERE BOARD_TYPE = 'map'");
		s.execute("INSERT INTO APP_SCHEMA_PATCH (PATCH_ID) VALUES ('" + PATCH_DELETE_MAP_BOARDS + "')");
	}

	private void dropUniqueIndexOnSingleColumn(Connection c, Statement s, String schema, String tableName, String columnName) {
		try {
			DatabaseMetaData md = c.getMetaData();

			// indexName -> columns (in order)
			Map<String, List<String>> indexColumns = new LinkedHashMap<>();

			try (ResultSet rs = md.getIndexInfo(null, schema, tableName, true, false)) {
				while (rs.next()) {
					String indexName = rs.getString("INDEX_NAME");
					String col = rs.getString("COLUMN_NAME");
					if (indexName == null || col == null) continue;
					indexColumns.computeIfAbsent(indexName, k -> new ArrayList<>()).add(col);
				}
			}

			String target = columnName.toUpperCase(Locale.ROOT);
			for (Map.Entry<String, List<String>> e : indexColumns.entrySet()) {
				List<String> cols = e.getValue();
				if (cols.size() != 1) continue;
				if (!target.equals(cols.get(0).toUpperCase(Locale.ROOT))) continue;

				String indexName = e.getKey();
				try {
					s.execute("DROP INDEX IF EXISTS " + indexName);
				} catch (Exception ignored) {
				}
				try {
					s.execute("DROP INDEX IF EXISTS " + schema + "." + indexName);
				} catch (Exception ignored) {
				}
			}
		} catch (Exception ignored) {
		}
	}
}

