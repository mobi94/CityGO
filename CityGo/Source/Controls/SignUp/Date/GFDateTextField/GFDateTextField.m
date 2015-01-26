//
//  GFDateTextField.m
//  Gracefull
//
//  Created by curly0nion on 9/23/14.
//  Copyright (c) 2014 NIX. All rights reserved.
//

#import "GFDateTextField.h"

@implementation GFDateTextField

- (NSString *)text
{
    NSDateFormatter *dateFormatter = [NSDateFormatter new];
    [dateFormatter setDateStyle:NSDateFormatterLongStyle];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    
    NSString *dateString = [super text];
    NSDate *dateToFormat = [dateFormatter dateFromString:dateString];
    
    [dateFormatter setDateFormat:@"yyyy.MM.dd"];
    
    return [dateFormatter stringFromDate:dateToFormat];
}

@end
