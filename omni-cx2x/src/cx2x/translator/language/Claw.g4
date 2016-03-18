/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */


/**
 * ANTLR 4 Grammar file for the CLAW directive language.
 *
 * @author clementval
 */
grammar Claw;

@header
{
import java.util.List;
import java.util.ArrayList;
import cx2x.translator.common.Constant;
import cx2x.translator.misc.Utility;
}

/*----------------------------------------------------------------------------
 * PARSER RULES
 *----------------------------------------------------------------------------*/


/*
 * Entry point for the analyzis of a CLAW directive.
 * Return a CLawLanguage object with all needed information.
 */
analyze returns [ClawLanguage l]
  @init{
    $l = new ClawLanguage();
  }
  :
  CLAW directive[$l]
;


ids_list[List<String> ids]
  :
    i=IDENTIFIER { $ids.add($i.text); }
  | i=IDENTIFIER { $ids.add($i.text); } ',' ids_list[$ids]
;

directive[ClawLanguage l]
  @init{
    List<ClawMapping> m = new ArrayList<>();
    List<String> o = new ArrayList<>();
  }
  :

  // loop-fusion directive
    LFUSION { $l.setDirective(ClawDirective.LOOP_FUSION); } group_option[$l] EOF

  // loop-interchange directive
  | LINTERCHANGE { $l.setDirective(ClawDirective.LOOP_INTERCHANGE); } indexes_option[$l] EOF

  // loop-extract directive
  | LEXTRACT range_option mapping_option_list[m] fusion_optional[$l] parallel_optional[$l] acc_optional[$l] EOF
    {
      $l.setDirective(ClawDirective.LOOP_EXTRACT);
      $l.setRange($range_option.r);
      $l.setMappings(m);
    }

  // remove directive
  | REMOVE { $l.setDirective(ClawDirective.REMOVE); } EOF
  | END REMOVE { $l.setDirective(ClawDirective.END_REMOVE); } EOF

  // Kcache directive
  | KCACHE offset_list_optional[o] EOF
    {
      $l.setDirective(ClawDirective.KCACHE);
      $l.setOffsets(o);
    }

  // Array notation transformation directive
  | ARRAY_TRANS fusion_optional[$l] parallel_optional[$l] acc_optional[$l] EOF
    {
      $l.setDirective(ClawDirective.ARRAY_TRANSFORM);
    }
;

group_option[ClawLanguage l]:
    GROUP '(' group_name=IDENTIFIER ')'
    { $l.setGroupOption($group_name.text); }
  | /* empty */
;

fusion_optional[ClawLanguage l]:
    FUSION group_option[$l] { $l.setFusionOption(); }
  | /* empty */
;

parallel_optional[ClawLanguage l]:
    PARALLEL { $l.setParallelOption(); }
  | /* empty */
;

acc_optional[ClawLanguage l]
  @init{
    List<String> tempAcc = new ArrayList<>();
  }
  :
    ACC '(' identifiers[tempAcc] ')' { $l.setAccClauses(Utility.join(" ", tempAcc)); }
  | /* empty */
;

identifiers[List<String> ids]:
    i=IDENTIFIER { $ids.add($i.text); }
  | i=IDENTIFIER { $ids.add($i.text); } identifiers[$ids]
;


indexes_option[ClawLanguage l]
  @init{
    List<String> indexes = new ArrayList();
  }
  :
    '(' ids_list[indexes] ')' { $l.setIndexes(indexes); }
  | /* empty */
;

offset_list_optional[List<String> offsets]:
    offset_list[$offsets]
  | /* empty */
;

offset_list[List<String> offsets]:
    offset[$offsets]
  | offset[$offsets] offset_list[$offsets]
;

offset[List<String> offsets]:
    n=NUMBER { $offsets.add($n.text); }
  | '-' n=NUMBER { $offsets.add("-" + $n.text); }
  | '+' n=NUMBER { $offsets.add($n.text); }
;


range_option returns [ClawRange r]
  @init{
    $r = new ClawRange();
  }
  :
    RANGE '(' induction=IDENTIFIER '=' lower=range_id ',' upper=range_id ')'
    {
      $r.setInductionVar($induction.text);
      $r.setLowerBound($lower.text);
      $r.setUpperBound($upper.text);
      $r.setStep(Constant.DEFAULT_STEP_VALUE);
    }
  | RANGE '(' induction=IDENTIFIER '=' lower=range_id ',' upper=range_id ',' step=range_id ')'
    {
      $r.setInductionVar($induction.text);
      $r.setLowerBound($lower.text);
      $r.setUpperBound($upper.text);
      $r.setStep($step.text);
    }
;

range_id returns [String text]:
    n=NUMBER { $text = $n.text; }
  | i=IDENTIFIER { $text = $i.text; }
;


mapping_var returns [ClawMappingVar mappingVar]:
    lhs=IDENTIFIER '/' rhs=IDENTIFIER
    {
      $mappingVar = new ClawMappingVar($lhs.text, $rhs.text);
    }
  | i=IDENTIFIER
    {
      $mappingVar = new ClawMappingVar($i.text, $i.text);
    }
;


mapping_var_list[List<ClawMappingVar> vars]:
     mv=mapping_var { $vars.add($mv.mappingVar); }
   | mv=mapping_var { $vars.add($mv.mappingVar); } ',' mapping_var_list[$vars]
;


mapping_option returns [ClawMapping mapping]
  @init{
    $mapping = new ClawMapping();
    List<ClawMappingVar> listMapped = new ArrayList<ClawMappingVar>();
    List<ClawMappingVar> listMapping = new ArrayList<ClawMappingVar>();
    $mapping.setMappedVariables(listMapped);
    $mapping.setMappingVariables(listMapping);
  }
  :
    MAP '(' mapping_var_list[listMapped] ':' mapping_var_list[listMapping] ')'
;

mapping_option_list[List<ClawMapping> mappings]:
    m=mapping_option { $mappings.add($m.mapping); }
  | m=mapping_option { $mappings.add($m.mapping); } mapping_option_list[$mappings]
;

/*----------------------------------------------------------------------------
 * LEXER RULES
 *----------------------------------------------------------------------------*/

// Start point
CLAW         : 'claw';

// Directives
ARRAY_TRANS  : 'array-transform';
END          : 'end';
KCACHE       : 'kcache';
LFUSION      : 'loop-fusion';
LINTERCHANGE : 'loop-interchange';
LEXTRACT     : 'loop-extract';
REMOVE       : 'remove';

// Options
ACC          : 'acc';
FUSION       : 'fusion';
GROUP        : 'group';
MAP          : 'map';
PARALLEL     : 'parallel';
RANGE        : 'range';

// Special elements
IDENTIFIER      : [a-zA-Z_$] [a-zA-Z_$0-9]* ;
NUMBER          : (DIGIT)+ ;
fragment DIGIT  : [0-9] ;

// Skip whitspaces
WHITESPACE   : ( '\t' | ' ' | '\r' | '\n'| '\u000C' )+ { skip(); };