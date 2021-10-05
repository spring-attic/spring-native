#!/bin/bash
set -e

source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository

pushd git-repo > /dev/null
git fetch --tags --all > /dev/null
popd > /dev/null

git clone git-repo stage-git-repo > /dev/null

pushd stage-git-repo > /dev/null

snapshotVersion=$( get_revision_from_pom )
if [[ $RELEASE_TYPE = "M" ]]; then
	stageVersion=$( get_next_milestone_release $snapshotVersion)
	nextVersion=$snapshotVersion
elif [[ $RELEASE_TYPE = "RC" ]]; then
	stageVersion=$( get_next_rc_release $snapshotVersion)
	nextVersion=$snapshotVersion
elif [[ $RELEASE_TYPE = "RELEASE" ]]; then
	stageVersion=$( get_next_release $snapshotVersion)
	nextVersion=$( bump_version_number $snapshotVersion)
else
	echo "Unknown release type $RELEASE_TYPE" >&2; exit 1;
fi

echo "Staging $stageVersion (next version will be $nextVersion)"
set_revision_to_pom "$stageVersion"

git config user.name "Spring Builds" > /dev/null
git config user.email "spring-builds@users.noreply.github.com" > /dev/null
git add pom.xml > /dev/null
git commit -m"Release $stageVersion" > /dev/null
git tag -a "$stageVersion" -m"Release $stageVersion" > /dev/null

./mvnw clean deploy -U -Pdocs -DaltDeploymentRepository=distribution::default::file://${repository}

git reset --hard HEAD^ > /dev/null
if [[ $nextVersion != $snapshotVersion ]]; then
	echo "Setting next development version ($nextVersion)"
	set_revision_to_pom "$nextVersion"
	git add pom.xml > /dev/null
	git commit -m"Next development version ($nextVersion)" > /dev/null
fi;

echo "DONE"

popd > /dev/null
