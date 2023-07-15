/*
 * Copyright (C) 15/07/23, 9:22 pm KUKODE - Kuchuk Boram Debbarma . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package dev.kukode.services.commit;

import dev.kukode.models.KuflexRepoModel;
import dev.kukode.models.commits.CommitModel;

public interface ICommitService {

    CommitModel loadActiveCommitFromKuflexrepo(String projectDir, KuflexRepoModel kuflexRepoModel);
}
