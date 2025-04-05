-- KEYS[1]: lock key
-- ARGV[1]: lock value (thread id)

-- 키가 존재하고 값이 일치하는 경우에만 삭제
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1]) > 0
else
    return false
end