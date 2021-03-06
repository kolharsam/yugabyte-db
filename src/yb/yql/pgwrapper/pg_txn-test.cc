// Copyright (c) YugaByte, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software distributed under the License
// is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied.  See the License for the specific language governing permissions and limitations
// under the License.
//

#include "yb/yql/pgwrapper/pg_mini_test_base.h"

#include "yb/util/test_macros.h"

DECLARE_bool(TEST_fail_in_apply_if_no_metadata);

namespace yb {
namespace pgwrapper {

class PgTxnTest : public PgMiniTestBase {

};

TEST_F(PgTxnTest, YB_DISABLE_TEST_IN_SANITIZERS(EmptyUpdate)) {
  FLAGS_TEST_fail_in_apply_if_no_metadata = true;

  auto conn = ASSERT_RESULT(Connect());

  ASSERT_OK(conn.Execute("CREATE TABLE test (key TEXT, value TEXT, PRIMARY KEY((key) HASH))"));
  ASSERT_OK(conn.StartTransaction(IsolationLevel::SNAPSHOT_ISOLATION));
  ASSERT_OK(conn.Execute("UPDATE test SET value = 'a' WHERE key = 'b'"));
  ASSERT_OK(conn.CommitTransaction());
}

class PgTxnRF1Test : public PgTxnTest {
 public:
  int NumTabletServers() override {
    return 1;
  }
};

TEST_F_EX(PgTxnTest, YB_DISABLE_TEST_IN_TSAN(SelectRF1ReadOnlyDeferred), PgTxnRF1Test) {
  auto conn = ASSERT_RESULT(Connect());

  ASSERT_OK(conn.Execute("CREATE TABLE test (key INT)"));
  ASSERT_OK(conn.Execute("INSERT INTO test VALUES (1)"));
  ASSERT_OK(conn.Execute("BEGIN ISOLATION LEVEL SERIALIZABLE, READ ONLY, DEFERRABLE"));
  auto res = ASSERT_RESULT(conn.FetchValue<int32_t>("SELECT * FROM test"));
  ASSERT_EQ(res, 1);
  ASSERT_OK(conn.Execute("COMMIT"));
}

} // namespace pgwrapper
} // namespace yb
