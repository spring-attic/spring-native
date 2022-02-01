/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hibernate.validator;

import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.GenericBootstrap;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidators.RegexpURLValidator;
import org.hibernate.validator.internal.constraintvalidators.AbstractEmailValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.AssertFalseValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.AssertTrueValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.NullValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.money.CurrencyValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.DecimalMaxValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.DecimalMinValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.DigitsValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.MaxValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.MinValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.NegativeOrZeroValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.NegativeValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.PositiveOrZeroValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.money.PositiveValidatorForMonetaryAmount;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArray;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfBoolean;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfByte;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfChar;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfInt;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfLong;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForArraysOfShort;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForCollection;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForMap;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.AbstractMaxValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.AbstractMinValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.AbstractDecimalMaxValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.AbstractDecimalMinValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArray;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfBoolean;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfByte;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfChar;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfInt;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfLong;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfShort;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCollection;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForMap;
import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractEpochBasedTimeValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractInstantBasedTimeValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.AbstractJavaTimeValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.AbstractFutureEpochBasedValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.AbstractFutureInstantBasedValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.AbstractFutureJavaTimeValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForHijrahDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForJapaneseDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForLocalDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForLocalDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForLocalTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForMinguoDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForMonthDay;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForOffsetDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForOffsetTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForThaiBuddhistDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForYear;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForYearMonth;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.FutureValidatorForZonedDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.AbstractFutureOrPresentEpochBasedValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.AbstractFutureOrPresentInstantBasedValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.AbstractFutureOrPresentJavaTimeValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForHijrahDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForJapaneseDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForLocalDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForLocalDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForLocalTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForMinguoDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForMonthDay;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForOffsetDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForOffsetTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForThaiBuddhistDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForYear;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForYearMonth;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForZonedDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.AbstractPastEpochBasedValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.AbstractPastInstantBasedValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.AbstractPastJavaTimeValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForHijrahDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForJapaneseDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForLocalDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForLocalDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForLocalTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForMinguoDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForMonthDay;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForOffsetDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForOffsetTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForThaiBuddhistDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForYear;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForYearMonth;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.PastValidatorForZonedDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.AbstractPastOrPresentEpochBasedValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.AbstractPastOrPresentInstantBasedValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.AbstractPastOrPresentJavaTimeValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForHijrahDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForJapaneseDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForLocalDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForLocalDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForLocalTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForMinguoDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForMonthDay;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForOffsetDateTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForOffsetTime;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForReadableInstant;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForReadablePartial;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForThaiBuddhistDate;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForYear;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForYearMonth;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.PastOrPresentValidatorForZonedDateTime;
import org.hibernate.validator.internal.constraintvalidators.hv.CodePointLengthValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.EANValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.ISBNValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.LengthValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.LuhnCheckValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod10CheckValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.NormalizedValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.URLValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.UniqueElementsValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.br.CNPJValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.pl.NIPValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.pl.PESELValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.pl.PolishNumberValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.pl.REGONValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMaxValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMinValidator;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.internal.engine.resolver.JPATraversableResolver;
import org.hibernate.validator.internal.engine.resolver.TraversableResolvers;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.Log_$logger;
import org.hibernate.validator.internal.util.logging.Messages;
import org.hibernate.validator.internal.util.logging.Messages_$bundle;
import org.hibernate.validator.internal.xml.config.ValidationBootstrapParameters;
import org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.InitializationHint;
import org.springframework.nativex.hint.InitializationTime;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(trigger = ValidatorImpl.class,
		resources = @ResourceHint(patterns="org.hibernate.validator.ValidationMessages", isBundle = true),
		jdkProxies = @JdkProxyHint(types = {
				javax.validation.Validator.class,
				org.springframework.aop.SpringProxy.class,
				org.springframework.aop.framework.Advised.class,
				org.springframework.core.DecoratingProxy.class
		}),
		types = { @TypeHint(types = {
				ParameterMessageInterpolator.class,
				HibernateValidatorConfiguration.class,
				AbstractMessageInterpolator.class,
				JPATraversableResolver.class,
				TraversableResolvers.class,
				PlatformResourceBundleLocator.class,
				ConfigurationImpl.class,
				TraversableResolvers.class,
				Messages.class,
				ParameterizedMessageFactory.class,
				DefaultFlowMessageFactory.class,
				ValidationBootstrapParameters.class,
				HibernateValidatorConfiguration.class,
				ConfigurationImpl.class,
				ConstraintDescriptorImpl.class,
				NotEmptyValidatorForCharSequence.class,
				DigitsValidatorForCharSequence.class,
				GenericBootstrap.class,
				PatternValidator.class,
				ReusableMessageFactory.class,
				NotNullValidator.class,
				Log_$logger.class,
				Log.class
		}, typeNames = {
				"org.hibernate.validator.internal.engine.resolver.TraverseAllTraversableResolver",
		}),
				@TypeHint(types = Messages_$bundle.class, access = { TypeAccess.DECLARED_CONSTRUCTORS, TypeAccess.DECLARED_FIELDS}),
				@TypeHint(
					types = javax.validation.Validator.class,
					typeNames = "java.lang.Module",
						access = TypeAccess.DECLARED_CONSTRUCTORS),
				@TypeHint(types = {
						ValidatorFactory.class,
						Pattern.class,
						AssertFalse.class,
						AssertTrue.class,
						DecimalMax.class,
						DecimalMin.class,
						Digits.class,
						Email.class,
						Future.class,
						FutureOrPresent.class,
						Max.class,
						Min.class,
						Negative.class,
						NegativeOrZero.class,
						NotBlank.class,
						NotEmpty.class,
						NotNull.class,
						Null.class,
						Past.class,
						PastOrPresent.class,
						Pattern.class,
						Positive.class,
						PositiveOrZero.class,
						Size.class
				}),
				// TODO Implement more clever dynamic hints to just use what is needed based on annotations used for example
				@TypeHint(types = {
						HibernateConstraintValidator.class,
						RegexpURLValidator.class,
						AbstractEmailValidator.class,
						AssertFalseValidator.class,
						AssertTrueValidator.class,
						DigitsValidatorForCharSequence.class,
						DigitsValidatorForNumber.class,
						EmailValidator.class,
						NotBlankValidator.class,
						NotNullValidator.class,
						NullValidator.class,
						PatternValidator.class,
						CurrencyValidatorForMonetaryAmount.class,
						DecimalMaxValidatorForMonetaryAmount.class,
						DecimalMinValidatorForMonetaryAmount.class,
						DigitsValidatorForMonetaryAmount.class,
						MaxValidatorForMonetaryAmount.class,
						MinValidatorForMonetaryAmount.class,
						NegativeOrZeroValidatorForMonetaryAmount.class,
						NegativeValidatorForMonetaryAmount.class,
						PositiveOrZeroValidatorForMonetaryAmount.class,
						PositiveValidatorForMonetaryAmount.class,
						NotEmptyValidatorForArray.class,
						NotEmptyValidatorForArraysOfBoolean.class,
						NotEmptyValidatorForArraysOfByte.class,
						NotEmptyValidatorForArraysOfChar.class,
						NotEmptyValidatorForArraysOfDouble.class,
						NotEmptyValidatorForArraysOfFloat.class,
						NotEmptyValidatorForArraysOfInt.class,
						NotEmptyValidatorForArraysOfLong.class,
						NotEmptyValidatorForArraysOfShort.class,
						NotEmptyValidatorForCharSequence.class,
						NotEmptyValidatorForCollection.class,
						NotEmptyValidatorForMap.class,
						AbstractMaxValidator.class,
						AbstractMinValidator.class,
						MaxValidatorForBigDecimal.class,
						MaxValidatorForBigInteger.class,
						MaxValidatorForByte.class,
						MaxValidatorForCharSequence.class,
						MaxValidatorForDouble.class,
						MaxValidatorForFloat.class,
						MaxValidatorForInteger.class,
						MaxValidatorForLong.class,
						MaxValidatorForNumber.class,
						MaxValidatorForShort.class,
						MinValidatorForBigDecimal.class,
						MinValidatorForBigInteger.class,
						MinValidatorForByte.class,
						MinValidatorForCharSequence.class,
						MinValidatorForDouble.class,
						MinValidatorForFloat.class,
						MinValidatorForInteger.class,
						MinValidatorForLong.class,
						MinValidatorForNumber.class,
						MinValidatorForShort.class,
						AbstractDecimalMaxValidator.class,
						AbstractDecimalMinValidator.class,
						DecimalMaxValidatorForBigDecimal.class,
						DecimalMaxValidatorForBigInteger.class,
						DecimalMaxValidatorForByte.class,
						DecimalMaxValidatorForCharSequence.class,
						DecimalMaxValidatorForDouble.class,
						DecimalMaxValidatorForFloat.class,
						DecimalMaxValidatorForInteger.class,
						DecimalMaxValidatorForLong.class,
						DecimalMaxValidatorForNumber.class,
						DecimalMaxValidatorForShort.class,
						DecimalMinValidatorForBigDecimal.class,
						DecimalMinValidatorForBigInteger.class,
						DecimalMinValidatorForByte.class,
						DecimalMinValidatorForCharSequence.class,
						DecimalMinValidatorForDouble.class,
						DecimalMinValidatorForFloat.class,
						DecimalMinValidatorForInteger.class,
						DecimalMinValidatorForLong.class,
						DecimalMinValidatorForNumber.class,
						DecimalMinValidatorForShort.class,
						NegativeOrZeroValidatorForBigDecimal.class,
						NegativeOrZeroValidatorForBigInteger.class,
						NegativeOrZeroValidatorForByte.class,
						NegativeOrZeroValidatorForCharSequence.class,
						NegativeOrZeroValidatorForDouble.class,
						NegativeOrZeroValidatorForFloat.class,
						NegativeOrZeroValidatorForInteger.class,
						NegativeOrZeroValidatorForLong.class,
						NegativeOrZeroValidatorForNumber.class,
						NegativeOrZeroValidatorForShort.class,
						NegativeValidatorForBigDecimal.class,
						NegativeValidatorForBigInteger.class,
						NegativeValidatorForByte.class,
						NegativeValidatorForCharSequence.class,
						NegativeValidatorForDouble.class,
						NegativeValidatorForFloat.class,
						NegativeValidatorForInteger.class,
						NegativeValidatorForLong.class,
						NegativeValidatorForNumber.class,
						NegativeValidatorForShort.class,
						PositiveOrZeroValidatorForBigDecimal.class,
						PositiveOrZeroValidatorForBigInteger.class,
						PositiveOrZeroValidatorForByte.class,
						PositiveOrZeroValidatorForCharSequence.class,
						PositiveOrZeroValidatorForDouble.class,
						PositiveOrZeroValidatorForFloat.class,
						PositiveOrZeroValidatorForInteger.class,
						PositiveOrZeroValidatorForLong.class,
						PositiveOrZeroValidatorForNumber.class,
						PositiveOrZeroValidatorForShort.class,
						PositiveValidatorForBigDecimal.class,
						PositiveValidatorForBigInteger.class,
						PositiveValidatorForByte.class,
						PositiveValidatorForCharSequence.class,
						PositiveValidatorForDouble.class,
						PositiveValidatorForFloat.class,
						PositiveValidatorForInteger.class,
						PositiveValidatorForLong.class,
						PositiveValidatorForNumber.class,
						PositiveValidatorForShort.class,
						SizeValidatorForArray.class,
						SizeValidatorForArraysOfBoolean.class,
						SizeValidatorForArraysOfByte.class,
						SizeValidatorForArraysOfChar.class,
						SizeValidatorForArraysOfDouble.class,
						SizeValidatorForArraysOfFloat.class,
						SizeValidatorForArraysOfInt.class,
						SizeValidatorForArraysOfLong.class,
						SizeValidatorForArraysOfShort.class,
						SizeValidatorForCharSequence.class,
						SizeValidatorForCollection.class,
						SizeValidatorForMap.class,
						AbstractEpochBasedTimeValidator.class,
						AbstractInstantBasedTimeValidator.class,
						AbstractJavaTimeValidator.class,
						AbstractFutureEpochBasedValidator.class,
						AbstractFutureInstantBasedValidator.class,
						AbstractFutureJavaTimeValidator.class,
						FutureValidatorForCalendar.class,
						FutureValidatorForDate.class,
						FutureValidatorForHijrahDate.class,
						FutureValidatorForInstant.class,
						FutureValidatorForJapaneseDate.class,
						FutureValidatorForLocalDate.class,
						FutureValidatorForLocalDateTime.class,
						FutureValidatorForLocalTime.class,
						FutureValidatorForMinguoDate.class,
						FutureValidatorForMonthDay.class,
						FutureValidatorForOffsetDateTime.class,
						FutureValidatorForOffsetTime.class,
						FutureValidatorForReadableInstant.class,
						FutureValidatorForReadablePartial.class,
						FutureValidatorForThaiBuddhistDate.class,
						FutureValidatorForYear.class,
						FutureValidatorForYearMonth.class,
						FutureValidatorForZonedDateTime.class,
						AbstractFutureOrPresentEpochBasedValidator.class,
						AbstractFutureOrPresentInstantBasedValidator.class,
						AbstractFutureOrPresentJavaTimeValidator.class,
						FutureOrPresentValidatorForCalendar.class,
						FutureOrPresentValidatorForDate.class,
						FutureOrPresentValidatorForHijrahDate.class,
						FutureOrPresentValidatorForInstant.class,
						FutureOrPresentValidatorForJapaneseDate.class,
						FutureOrPresentValidatorForLocalDate.class,
						FutureOrPresentValidatorForLocalDateTime.class,
						FutureOrPresentValidatorForLocalTime.class,
						FutureOrPresentValidatorForMinguoDate.class,
						FutureOrPresentValidatorForMonthDay.class,
						FutureOrPresentValidatorForOffsetDateTime.class,
						FutureOrPresentValidatorForOffsetTime.class,
						FutureOrPresentValidatorForReadableInstant.class,
						FutureOrPresentValidatorForReadablePartial.class,
						FutureOrPresentValidatorForThaiBuddhistDate.class,
						FutureOrPresentValidatorForYear.class,
						FutureOrPresentValidatorForYearMonth.class,
						FutureOrPresentValidatorForZonedDateTime.class,
						AbstractPastEpochBasedValidator.class,
						AbstractPastInstantBasedValidator.class,
						AbstractPastJavaTimeValidator.class,
						PastValidatorForCalendar.class,
						PastValidatorForDate.class,
						PastValidatorForHijrahDate.class,
						PastValidatorForInstant.class,
						PastValidatorForJapaneseDate.class,
						PastValidatorForLocalDate.class,
						PastValidatorForLocalDateTime.class,
						PastValidatorForLocalTime.class,
						PastValidatorForMinguoDate.class,
						PastValidatorForMonthDay.class,
						PastValidatorForOffsetDateTime.class,
						PastValidatorForOffsetTime.class,
						PastValidatorForReadableInstant.class,
						PastValidatorForReadablePartial.class,
						PastValidatorForThaiBuddhistDate.class,
						PastValidatorForYear.class,
						PastValidatorForYearMonth.class,
						PastValidatorForZonedDateTime.class,
						AbstractPastOrPresentEpochBasedValidator.class,
						AbstractPastOrPresentInstantBasedValidator.class,
						AbstractPastOrPresentJavaTimeValidator.class,
						PastOrPresentValidatorForCalendar.class,
						PastOrPresentValidatorForDate.class,
						PastOrPresentValidatorForHijrahDate.class,
						PastOrPresentValidatorForInstant.class,
						PastOrPresentValidatorForJapaneseDate.class,
						PastOrPresentValidatorForLocalDate.class,
						PastOrPresentValidatorForLocalDateTime.class,
						PastOrPresentValidatorForLocalTime.class,
						PastOrPresentValidatorForMinguoDate.class,
						PastOrPresentValidatorForMonthDay.class,
						PastOrPresentValidatorForOffsetDateTime.class,
						PastOrPresentValidatorForOffsetTime.class,
						PastOrPresentValidatorForReadableInstant.class,
						PastOrPresentValidatorForReadablePartial.class,
						PastOrPresentValidatorForThaiBuddhistDate.class,
						PastOrPresentValidatorForYear.class,
						PastOrPresentValidatorForYearMonth.class,
						PastOrPresentValidatorForZonedDateTime.class,
						// Disabled because of a com.oracle.truffle.js.scriptengine.GraalJSEngineFactory.getEngineVersion(GraalJSEngineFactory.java:132) error with GraalVM 21.0.0.2
						//AbstractScriptAssertValidator.class,
						CodePointLengthValidator.class,
						EANValidator.class,
						org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator.class,
						ISBNValidator.class,
						LengthValidator.class,
						LuhnCheckValidator.class,
						Mod10CheckValidator.class,
						Mod11CheckValidator.class,
						NormalizedValidator.class,
						org.hibernate.validator.internal.constraintvalidators.hv.NotBlankValidator.class,
						// Disabled because of a com.oracle.truffle.js.scriptengine.GraalJSEngineFactory.getEngineVersion(GraalJSEngineFactory.java:132) error with GraalVM 21.0.0.2
						// ParameterScriptAssertValidator.class,
						//ScriptAssertValidator.class,
						UniqueElementsValidator.class,
						URLValidator.class,
						CNPJValidator.class,
						CPFValidator.class,
						NIPValidator.class,
						PESELValidator.class,
						PolishNumberValidator.class,
						REGONValidator.class,
						DurationMaxValidator.class,
						DurationMinValidator.class
				})
		},
		initialization = @InitializationHint(types = {
				org.hibernate.validator.internal.util.logging.Log_$logger.class,
				org.hibernate.validator.internal.util.TypeHelper.class,
				org.hibernate.validator.internal.util.privilegedactions.LoadClass.class,
				org.hibernate.validator.internal.util.Contracts.class,
				org.hibernate.validator.internal.util.ReflectionHelper.class
		}, packageNames = "org.hibernate.validator.internal.engine.valueextraction", initTime = InitializationTime.BUILD)
)
@NativeHint(trigger = org.springframework.validation.beanvalidation.SpringValidatorAdapter.class,
	types = @TypeHint(
			typeNames = "org.springframework.validation.beanvalidation.SpringValidatorAdapter$ViolationFieldError",
			access = { TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS }
))
public class ValidatorHints implements NativeConfiguration {
}
