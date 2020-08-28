package constants

object DatasetMetadata {
  var sdgDatasets : Set[Map[String, String]] = Set(
    Map(
      "slug" -> "sdg-target-for-access-to-sanitation",
      "friendlyName" -> "Has country already reached SDG target for access to sanitation? (1990 - 2015)",
    ),
    Map(
      "slug" -> "sdg-target-on-child-mortality",
      "friendlyName" -> "Has country already reached SDG target on child mortality? (1975 - 2017)",
    ),
    Map(
      "slug" -> "sdg-target-on-maternal-mortality",
    ),
    Map(
      "slug" -> "sdg-target-on-neonatal-mortality",
    ),
    Map(
      "slug" -> "life-expectancy",
    ),
  )
  var sanitationDatasets : Set[Map[String, String]] = Set(
    Map(
      "slug" -> "people-practicing-open-defecation-in-urban-areas-of-urban-population",
    ),
	)
  var environmentDatasets : Set[Map[String, String]] = Set(
    Map(
      "slug" -> "forest-area-percent",
    ),
    Map(
      "slug" -> "above-ground-biomass-in-forest-per-hectare",
    ),
	)
  // there are also:
  // - urbanization-last-500-years (but cambodia only has from 1950)
  // - long-term-urban-population-region (goes back go 10,000 bc...but that is more to show global trends and some shock factor about global trends)
  // - urban-population-share-2050 (but share-of-population-urban doesn't have the fun guessing that OWID has! BUt who cares)
  var urbanizationDatasets : Set[Map[String, String]] = Set(
    Map(
      "slug" -> "share-of-population-urban",
    ),
	)
}
