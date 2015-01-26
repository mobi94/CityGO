//
//  NSDate+CompareDateOnly.m
//  Gracefull
//
//  Created by moskalenko on 1/8/15.
//  Copyright (c) 2015 NIX. All rights reserved.
//

#import "NSDate+CompareDateOnly.h"

@implementation NSDate (CompareDateOnly)

+ (NSComparisonResult)compareDateOnly:(NSString *)otherDate
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"dd-MM-yyyy"];
    NSDate *dateFromString = [[NSDate alloc] init];
    dateFromString = [dateFormatter dateFromString:otherDate];
    
    NSUInteger dateFlags = NSYearCalendarUnit|NSMonthCalendarUnit|NSDayCalendarUnit;
    NSCalendar *gregorianCalendar = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
    NSDateComponents *selfComponents = [gregorianCalendar components:dateFlags fromDate:[NSDate date]];
    NSDate *selfDateOnly = [gregorianCalendar dateFromComponents:selfComponents];
    
    NSDateComponents *otherCompents = [gregorianCalendar components:dateFlags fromDate:dateFromString];
    NSDate *otherDateOnly = [gregorianCalendar dateFromComponents:otherCompents];
    return [selfDateOnly compare:otherDateOnly];
}

@end
