package models.traversalbuilder

import com.ryanquey.intertextualitygraph.reference.{BookReference, ChapterReference, VerseReference,
  VerseRangeWithinChapter,
  ChapterRangeWithinBook,
}
case class GroupedRangeSets(
                             bookReferences : Set[BookReference],
                             chapterRanges : Set[ChapterRangeWithinBook],
                             verseRanges : Set[VerseRangeWithinChapter]
                           )