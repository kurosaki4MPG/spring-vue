package com.example.springvuebackend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  private static JavaClasses applicationClasses;

  @BeforeAll
  static void importApplicationClasses() {
    // 実装コードだけを対象にし、外部ライブラリの依存関係は設計ルールの判定から除外する。
    applicationClasses = new ClassFileImporter().importPath(Paths.get("target", "classes"));
  }

  @Test
  void controllersMustNotBypassServiceLayer() {
    // HTTP入出力を担当するControllerからRepository・Entityへの直接依存を禁止する。
    noClasses()
        .that()
        .resideInAnyPackage("..controller..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..repository..", "..entity..")
        .check(applicationClasses);
  }

  @Test
  void repositoriesMustNotDependOnUpperLayers() {
    // DBアクセスを担当するRepositoryからController・Serviceへの逆方向依存を禁止する。
    noClasses()
        .that()
        .resideInAnyPackage("..repository..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..controller..", "..service..")
        .check(applicationClasses);
  }

  @Test
  void servicesMustNotDependOnControllerLayer() {
    // 業務処理を担当するServiceからHTTP層への依存を禁止する。
    noClasses()
        .that()
        .resideInAnyPackage("..service..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..controller..")
        .check(applicationClasses);
  }

  @Test
  void dtosMustNotDependOnImplementationLayers() {
    // API契約を表すDTOからController・Service・Repository・Entityへの依存を禁止する。
    noClasses()
        .that()
        .resideInAnyPackage("..dto..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("..controller..", "..service..", "..repository..", "..entity..")
        .check(applicationClasses);
  }
}
